package com.topsail.reliable.message.bank2.mapper;

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

    @Update("update account set account_balance = account_balance + #{amount} where account_id = #{accountId}")
    int updateAccountBalance(@Param("accountId") String accountId, @Param("amount") Long amount);

}
