package com.topsail.reliable.message.bank2.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

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
@TableName("de_duplicate")
public class DeDuplicate {

    /**
     * 事务 Id
     */
    @TableId("transaction_id")
    String transactionId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    LocalDateTime createTime;

}
