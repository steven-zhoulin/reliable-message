package com.topsail.reliable.message.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Data
@Builder
@EqualsAndHashCode
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("account")
public class Account implements Serializable {

    /**
     * 事务 Id
     */
    @TableId("id")
    private Long id;

    /**
     * 账户号码
     */
    @TableField(value = "account_id")
    private String accountId;

    /**
     * 账户姓名
     */
    @TableField(value = "account_name")
    private String accountName;

    /**
     * 账户余额
     */
    @TableField(value = "account_balance")
    private Long accountBalance;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @TableField(value = "version")
    private Long version;


}