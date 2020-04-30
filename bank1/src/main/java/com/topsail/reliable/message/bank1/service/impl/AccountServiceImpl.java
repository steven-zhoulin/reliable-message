package com.topsail.reliable.message.bank1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank1.mapper.AccountMapper;
import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import com.topsail.reliable.message.core.Constants;
import com.topsail.reliable.message.core.convert.MessageConvert;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.core.entity.po.Account;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Slf4j
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Autowired
    private DeDuplicateService deDuplicateService;

    /**
     * 向 MQ 发送转账消息
     *
     * @param accountChangeEvent
     */
    @Override
    public void asyncUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {

        Message<String> message = MessageConvert.from(accountChangeEvent);

        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constants.PRODUCER_GROUP_BANK1, Constants.TOPIC_TRANSFER_ACCOUNT, message, null);
        String msgId = sendResult.getMsgId();

        log.info("发送转账消息成功！{}", msgId);
        log.info("{}", sendResult);

    }

    /**
     * 执行本地事务
     *
     * @param message
     * @param o
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void doExecuteLocalTransaction(Message message, Object o) {

        AccountChangeEvent accountChangeEvent = MessageConvert.from(message);

        // 幂等判断
        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        log.debug("开始处理本地事务...");

        // 扣减金额
        accountMapper.updateAccountBalance(accountChangeEvent.getFromAccountId(), accountChangeEvent.getAmount() * -1);

        // 添加事务日志
        DeDuplicate deDuplicate = DeDuplicate.builder()
            .transactionId(accountChangeEvent.getTransactionId())
            //.msgId(message.getHeaders())
            .createTime(LocalDateTime.now())
            .build();
        deDuplicateService.save(deDuplicate);

        log.debug("本地事务处理成功");

    }

}
