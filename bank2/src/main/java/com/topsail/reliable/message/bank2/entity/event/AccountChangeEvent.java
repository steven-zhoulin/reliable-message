package com.topsail.reliable.message.bank2.entity.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 账户变更事件
 *
 * @author Steven
 * @date 2020-02-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountChangeEvent implements Serializable {

    private String fromAccountNo;

    private String toAccountNo;

    /**
     * 变动金额
     */
    private Long amount;

    /**
     * 事务号
     */
    private String transactionId;

}
