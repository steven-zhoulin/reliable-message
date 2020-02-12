package com.topsail.reliable.message.bank2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.topsail.reliable.message.bank2.entity.po.Account;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Mapper
@Component
public interface AccountMapper extends BaseMapper<Account> {

    @Update("update account set account_balance = account_balance + #{amount} where account_no = #{accountNo}")
    int updateAccountBalance(@Param("accountNo") String accountNo, @Param("amount") Long amount);

}
