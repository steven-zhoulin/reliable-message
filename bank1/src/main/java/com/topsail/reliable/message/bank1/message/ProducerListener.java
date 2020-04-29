package com.topsail.reliable.message.bank1.message;

import com.alibaba.fastjson.JSONObject;
import com.topsail.reliable.message.bank1.Bank1Constants;
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

/**
 * @author Steven
 * @date 2020-02-12
 */
@Component
@Slf4j
@RocketMQTransactionListener(txProducerGroup = Bank1Constants.TRANSFER_GROUP)
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
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {

        log.info("===> 当前线程: {}", Thread.currentThread().getName());
        try {
            accountService.doExecuteLocalTransaction(message, o);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("本地事务执行异常: ", e);
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
