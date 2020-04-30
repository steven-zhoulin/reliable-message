package com.topsail.reliable.message.bank1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.core.entity.po.Account;
import org.springframework.messaging.Message;

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
     * 执行本地事务
     *
     * @param message
     * @param o
     */
    void doExecuteLocalTransaction(Message message, Object o) throws InterruptedException;

}
