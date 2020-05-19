package com.topsail.reliable.message.bank1.listener;

import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import com.topsail.reliable.message.core.Constants;
import com.topsail.reliable.message.core.convert.MessageConvert;
import com.topsail.reliable.message.core.entity.ExecuteStatus;
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

        AccountChangeEvent accountChangeEvent = MessageConvert.from(message);
        Object key = accountChangeEvent.getTransactionId();
        ExecuteStatus executeStatus = (ExecuteStatus) arg;

        try {
            accountService.doExecuteLocalTransaction(accountChangeEvent, arg);
            log.info("Local transaction execute success, commit half message! key: {}", key);
            executeStatus.setSuccess(true);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            executeStatus.setSuccess(false);
            log.error("Local transaction execute failure, rollback half message! key: {}", key);
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
        Object key = accountChangeEvent.getTransactionId();

        String transactionId = accountChangeEvent.getTransactionId();
        if (deDuplicateService.isExistTx(transactionId)) {
            log.info("Check local transaction execute success, commit half message! key: {}", key);
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            log.error("Check local transaction execute failure, rollback half message! key: {}", key);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

}
