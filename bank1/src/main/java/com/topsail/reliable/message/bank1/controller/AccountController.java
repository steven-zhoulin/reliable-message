package com.topsail.reliable.message.bank1.controller;

import com.topsail.reliable.message.bank1.service.AccountService;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.UUID;

/**
 * @author Steven
 * @date 2020-02-12
 */
@RestController
@Slf4j
@Validated
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * 转账
     *
     * @param amount 转账金额
     * @return
     */
    @GetMapping(value = "/transfer")
    public String transfer(@Min(value = 1, message = "amount must > 0")
                           @RequestParam("amount") Long amount) {

        // 事务ID，用于做幂等处理
        String transactionId = UUID.randomUUID().toString();
        AccountChangeEvent accountChangeEvent = AccountChangeEvent.builder()
            /** 银行卡账号：扣钱的账号 */
            .srcAccountId("6226-0000-1111-2222")
            /** 银行卡账号：充钱的账号 */
            .dstAccountId("9876-0000-0000-1111")
            .amount(amount)
            .transactionId(transactionId)
            .build();

        // 异步调用转账逻辑
        accountService.asyncUpdateAccountBalance(accountChangeEvent);
        return "转账成功";
    }

}
