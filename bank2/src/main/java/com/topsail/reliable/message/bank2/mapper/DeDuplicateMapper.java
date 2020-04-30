package com.topsail.reliable.message.bank2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Mapper
@Component
public interface DeDuplicateMapper extends BaseMapper<DeDuplicate> {

}
