package com.topsail.reliable.message.bank1.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.topsail.reliable.message.bank1.mapper.DeDuplicateMapper;
import com.topsail.reliable.message.bank1.service.DeDuplicateService;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author Steven
 * @date 2020-02-12
 */
@Slf4j
@Service
public class DeDuplicateServiceImpl extends ServiceImpl<DeDuplicateMapper, DeDuplicate> implements DeDuplicateService {

    @Autowired
    private DeDuplicateMapper deDuplicateMapper;

    /**
     * 判断事务是否处理过
     *
     * @param transactionId
     * @return
     */
    @Override
    public boolean isExistTx(String transactionId) {
        return count(
            Wrappers.<DeDuplicate>lambdaQuery()
                .eq(DeDuplicate::getTransactionId, transactionId)
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRED)
    public void updateTime() {
        log.info("==> 开始更新 deDuplicate 的时间戳");
        DeDuplicate deDuplicate = DeDuplicate.builder().transactionId("f3955126-a578-4344-8ea3-9bd698a9c760").createTime(LocalDateTime.now()).build();
        deDuplicateMapper.updateById(deDuplicate);
        //int i = 10 / 0;
        log.info("==> 结束更新 deDuplicate 的时间戳");
    }

}
