package com.topsail.reliable.message.bank1.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank1.mapper.AccountMapper;
import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import com.topsail.reliable.message.core.Constants;
import com.topsail.reliable.message.core.convert.MessageConvert;
import com.topsail.reliable.message.core.entity.ExecuteStatus;
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
import java.util.concurrent.TimeUnit;

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
        String destination = Constants.TOPIC_BANK1_ACCOUNT_CHANGE + ":" + accountChangeEvent.getDstBank();
        ExecuteStatus executeStatus = ExecuteStatus.builder().success(false).build();
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(Constants.PRODUCER_GROUP_BANK1, destination, message, executeStatus);
        log.info("本地事务执行成功与否: {}", executeStatus.getSuccess());
        log.info("topic: {} key: {}", Constants.TOPIC_BANK1_ACCOUNT_CHANGE, accountChangeEvent.getTransactionId());
        log.info("sendResult: {}", sendResult);

    }

    /**
     * 执行本地事务
     *
     * @param accountChangeEvent
     * @param o
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void doExecuteLocalTransaction(AccountChangeEvent accountChangeEvent, Object o) throws InterruptedException {

        // 1.幂等判断
        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        log.debug("开始处理本地事务...");

        // 基于乐观锁实现扣款，默认重试三次，每次间隔 500 毫秒
        int result = 0;
        for (int i = 0; i < Constants.OPTIMISTIC_LOCK_RETRY_TIMES; i++) {

            // 2.查询账户信息
            Account account = accountMapper.selectOne(
                Wrappers.<Account>lambdaQuery()
                    .eq(Account::getAccountId, accountChangeEvent.getSrcAccountId())
            );

            // 3.判断余额是否足够
            if (account.getAccountBalance() < accountChangeEvent.getAmount()) {
                throw new RuntimeException("余额不足!");
            }

            // 4.扣减金额
            result = accountMapper.updateAccountBalance(
                accountChangeEvent.getSrcAccountId(),
                Math.negateExact(accountChangeEvent.getAmount()),
                account.getVersion()
            );
            if (1 == result) {
                break;
            } else {
                log.info("并发扣款冲突，{}毫秒后重试", Constants.OPTIMISTIC_LOCK_RETRY_INTERVAL);
                TimeUnit.MICROSECONDS.sleep(Constants.OPTIMISTIC_LOCK_RETRY_INTERVAL);
            }
        }

        if (1 == result) {
            // 5.添加事务日志
            DeDuplicate deDuplicate = DeDuplicate.builder()
                .transactionId(accountChangeEvent.getTransactionId())
                //.msgId(message.getHeaders())
                .createTime(LocalDateTime.now())
                .build();
            deDuplicateService.save(deDuplicate);

            log.debug("本地事务处理成功");
        } else {
            throw new RuntimeException("转账失败: " + accountChangeEvent);
        }

    }

}
