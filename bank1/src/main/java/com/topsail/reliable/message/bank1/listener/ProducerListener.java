package com.topsail.reliable.message.bank1.listener;

import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import com.topsail.reliable.message.core.Constants;
import com.topsail.reliable.message.core.convert.MessageConvert;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
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
@RocketMQTransactionListener(txProducerGroup = Constants.PRODUCER_GROUP_BANK1)
public class ProducerListener implements RocketMQLocalTransactionListener {

    @Autowired
    AccountService accountService;

    @Autowired
    DeDuplicateService deDuplicateService;

    /**
     * 事务级 Half-消息 发送成功，在此执行本地事务，本地数据库事物执行时间不要超过 MQ 回查时间，第一次回查是：1分钟后。
     *
     * @param message
     * @param arg     sendMessageInTransaction 时带入的业务参数
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {

        try {
            accountService.doExecuteLocalTransaction(message, arg);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("本地事务执行异常: ", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }

    /**
     * 反查发送端本地事务是否提交（针对 Half-消息 长期未确认的情况）
     * 每分钟 1 次，默认 15 次
     *
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        log.info("反查发送端本地事务是否提交...");

        AccountChangeEvent accountChangeEvent = MessageConvert.from(message);

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