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
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        rocketMQTemplate.sendMessageInTransaction("producer_group_txmsg_bank1", "topic_txmsg", message, null);

    }

    /**
     * 更新账户，扣减金额
     *
     * @param accountChangeEvent
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {

        // 幂等判断
        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        // 扣减金额
        accountMapper.updateAccountBalance(accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount() * -1);
        //添加事务日志
        deDuplicateService.save(DeDuplicate.builder().transactionId(accountChangeEvent.getTransactionId()).build());
        if (accountChangeEvent.getAmount() == 3) {
            throw new RuntimeException("人为制造异常");
        }
    }

}
