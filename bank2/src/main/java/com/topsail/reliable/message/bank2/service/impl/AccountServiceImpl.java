package com.topsail.reliable.message.bank2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank2.mapper.AccountMapper;
import com.topsail.reliable.message.bank2.service.AccountService;
import com.topsail.reliable.message.bank2.service.DeDuplicateService;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.core.entity.po.Account;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DeDuplicateService deDuplicateService;

    @Autowired
    private AccountMapper accountMapper;

    /**
     * 更新账户，增加金额
     *
     * @param accountChangeEvent
     */
    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void addAccountInfoBalance(AccountChangeEvent accountChangeEvent) {

        log.info("bank2更新本地账号，账号：{}, 金额：{}", accountChangeEvent.getToAccountId(), accountChangeEvent.getAmount());

        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        if (accountChangeEvent.getAmount() < 0) {
            throw new RuntimeException("转账金额不能为负：" + accountChangeEvent.getAmount());
        }

        // 增加金额
        int result = accountMapper.updateAccountBalance(accountChangeEvent.getToAccountId(), accountChangeEvent.getAmount());
        if (1 == result) {
            // 添加事务记录，用于幂等判断
            DeDuplicate deDuplicate = DeDuplicate.builder()
                .transactionId(accountChangeEvent.getTransactionId())
                .createTime(LocalDateTime.now())
                .build();
            deDuplicateService.save(deDuplicate);
        } else {
            throw new RuntimeException("转账失败!" + accountChangeEvent);
        }

    }

}
