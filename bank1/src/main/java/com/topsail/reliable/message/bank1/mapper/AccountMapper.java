package com.topsail.reliable.message.bank1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.topsail.reliable.message.core.entity.po.Account;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Mapper
@Component
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 特别留意乐观锁的处理：必须在前面查询出余额的基础上进行扣减。
     *
     * @param accountId
     * @param amount
     * @param version 版本号
     * @return
     */
    @Update("update account set account_balance = account_balance + #{amount}, update_time = now(), version = version + 1 where account_id = #{accountId} and version = #{version}")
    int updateAccountBalance(@Param("accountId") String accountId, @Param("amount") Long amount, @Param("version") Long version);

}
