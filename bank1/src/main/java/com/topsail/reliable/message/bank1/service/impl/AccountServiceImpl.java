package com.topsail.reliable.message.bank1.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank1.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank1.entity.po.Account;
import com.topsail.reliable.message.bank1.entity.po.DeDuplicate;
import com.topsail.reliable.message.bank1.mapper.AccountMapper;
import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accountChange", accountChangeEvent);
        String jsonString = jsonObject.toJSONString();

        Message<String> message = MessageBuilder.withPayload(jsonString).build();

        /**
         * String txProducerGroup 生产组
         * String destination topic，
         * Message<?> message, 消息内容
         * Object arg 参数
         */
        log.info("===> 当前线程: {}", Thread.currentThread().getName());
        TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction("producer_group_txmsg_bank1", "topic_txmsg", message, null);
        String msgId = transactionSendResult.getMsgId();
        log.info("msgId: {}, 当前线程: {}", msgId, Thread.currentThread().getName());

    }

    /**
     * 更新账户，扣减金额
     *
     * @param accountChangeEvent
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {

        log.info("异步调用方，处理本地事务...");

        // 幂等判断
        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        // 扣减金额
        accountMapper.updateAccountBalance(accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount() * -1);

        log.info("添加事务日志");
        DeDuplicate deDuplicate = DeDuplicate.builder()
            .transactionId(accountChangeEvent.getTransactionId())
            .createTime(LocalDateTime.now())
            .build();
        deDuplicateService.save(deDuplicate);
        if (accountChangeEvent.getAmount() == 3) {
            throw new RuntimeException("人为制造异常");
        }
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
        String messageString = new String((byte[]) message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String accountChangeString = jsonObject.getString("accountChange");

        // 将accountChange（json）转成AccountChangeEvent
        AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);

        // 执行本地事务，扣减金额
        this.doUpdateAccountBalance(accountChangeEvent);
        int i = 10 / 0;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void test() {
        Account account = Account.builder().accountNo("1").accountName("啊哈哈").build();
        log.info("==＞　开始更新账户名称");
        accountMapper.updateById(account);
        log.info("==＞　结束更新账户名称");

            deDuplicateService.updateTime();

    }

}
