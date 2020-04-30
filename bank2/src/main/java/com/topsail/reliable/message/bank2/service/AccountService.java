package com.topsail.reliable.message.bank2.service;


import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;

/**
 * @author Steven
 * @date 2020-02-12
 */
public interface AccountService {

    /**
     * 更新账户，增加金额
     *
     * @param accountChangeEvent
     */
    void addAccountInfoBalance(AccountChangeEvent accountChangeEvent);

}
