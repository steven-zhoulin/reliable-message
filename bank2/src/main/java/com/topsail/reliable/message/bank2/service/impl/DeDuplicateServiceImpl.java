package com.topsail.reliable.message.bank2.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank2.mapper.DeDuplicateMapper;
import com.topsail.reliable.message.bank2.service.DeDuplicateService;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Slf4j
@Service
public class DeDuplicateServiceImpl extends ServiceImpl<DeDuplicateMapper, DeDuplicate> implements DeDuplicateService {

    @Override
    public boolean isExistTx(String transactionId) {
        return count(
            Wrappers.<DeDuplicate>lambdaQuery()
                .eq(DeDuplicate::getTransactionId, transactionId)
        ) > 0;
    }

}
