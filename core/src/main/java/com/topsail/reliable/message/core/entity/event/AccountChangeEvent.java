package com.topsail.reliable.message.core.entity.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 账户变更事件
 *
 * @author Steven
 * @date 2020-02-12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountChangeEvent implements Serializable {


    private String fromAccountId;

    private String toAccountId;

    /**
     * 变动金额
     */
    private Long amount;

    /**
     * 事务号
     */
    private String transactionId;

}