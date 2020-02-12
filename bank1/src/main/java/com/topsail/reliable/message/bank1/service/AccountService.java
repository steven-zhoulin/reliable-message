package com.topsail.reliable.message.bank1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.topsail.reliable.message.bank1.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank1.entity.po.Account;

/**
 * @author Steven
 * @date 2020-02-12
 */
public interface AccountService extends IService<Account> {

    /**
     * 异步调用转账逻辑
     *
     * @param accountChangeEvent
     */
    void asyncUpdateAccountBalance(AccountChangeEvent accountChangeEvent);

    /**
     * 更新账户，扣减金额
     *
     * @param accountChangeEvent
     */
    void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent);

}
