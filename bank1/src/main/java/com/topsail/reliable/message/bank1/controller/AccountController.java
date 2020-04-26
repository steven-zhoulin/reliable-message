package com.topsail.reliable.message.bank1.controller;

import com.topsail.reliable.message.bank1.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank1.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Steven
 * @date 2020-02-12
 */
@RestController
@Slf4j
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * 转账
     * @param accountId
     * @param amount
     * @return
     */
    @GetMapping(value = "/transfer")
    public String transfer(@RequestParam("accountId") String accountId, @RequestParam("amount") Long amount) {

        String transactionId = UUID.randomUUID().toString();
        AccountChangeEvent accountChangeEvent = new AccountChangeEvent(accountId, amount, transactionId);

        // 异步调用转账逻辑
        accountService.asyncUpdateAccountBalance(accountChangeEvent);
        return "转账成功";
    }
}
