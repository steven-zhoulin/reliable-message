package com.topsail.reliable.message.bank1.message;

import com.alibaba.fastjson.JSONObject;
import com.topsail.reliable.message.bank1.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Component
@Slf4j
@RocketMQTransactionListener(txProducerGroup = "producer_group_txmsg_bank1")
public class ProducerListener implements RocketMQLocalTransactionListener {

    @Autowired
    AccountService accountService;

    @Autowired
    DeDuplicateService deDuplicateService;

    /**
     * 事务级 Half-消息 发送成功，在此执行本地事务
     *
     * @param message
     * @param o
     * @return
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            String messageString = new String((byte[]) message.getPayload());
            JSONObject jsonObject = JSONObject.parseObject(messageString);
            String accountChangeString = jsonObject.getString("accountChange");

            // 将accountChange（json）转成AccountChangeEvent
            AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);

            // 执行本地事务，扣减金额
            accountService.doUpdateAccountBalance(accountChangeEvent);

            // 当返回 RocketMQLocalTransactionState.COMMIT，自动向 MQ 发送 commit 消息，MQ 将消息的状态改为可消费
            log.info("本地事务处理成功，向 MQ 发送 commit 指令！");
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("本地事务处理失败，向 MQ 发送 rollback 指令！");
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 反查发送端本地事务是否提交（针对 Half-消息 长期未确认的情况）
     *
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        log.info("反查发送端本地事务是否提交...");

        String messageString = new String((byte[]) message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String accountChangeString = jsonObject.getString("accountChange");

        AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);

        String transactionId = accountChangeEvent.getTransactionId();
        if (deDuplicateService.isExistTx(transactionId)) {
            log.info("调用端事务已提交，确认提交消息！");
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            log.info("调用端事务未提交，不提交消息！");
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

}
