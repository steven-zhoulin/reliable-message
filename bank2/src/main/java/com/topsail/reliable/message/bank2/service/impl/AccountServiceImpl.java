package com.topsail.reliable.message.bank2.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank2.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank2.entity.po.Account;
import com.topsail.reliable.message.bank2.entity.po.DeDuplicate;
import com.topsail.reliable.message.bank2.mapper.AccountMapper;
import com.topsail.reliable.message.bank2.service.AccountService;
import com.topsail.reliable.message.bank2.service.DeDuplicateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        log.info("bank2更新本地账号，账号：{},金额：{}", accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount());

        if (deDuplicateService.isExistTx(accountChangeEvent.getTransactionId())) {
            return;
        }

        // 增加金额
        accountMapper.updateAccountBalance(accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount());

        // 添加事务记录，用于幂等
        deDuplicateService.save(DeDuplicate.builder().transactionId(accountChangeEvent.getTransactionId()).build());
        if (accountChangeEvent.getAmount() == 4) {
            throw new RuntimeException("人为制造异常");
        }
    }

}
