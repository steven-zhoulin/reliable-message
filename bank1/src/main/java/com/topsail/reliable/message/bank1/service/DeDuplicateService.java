package com.topsail.reliable.message.bank1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.topsail.reliable.message.core.entity.po.DeDuplicate;

/**
 * @author Steven
 * @date 2020-02-12
 */
public interface DeDuplicateService extends IService<DeDuplicate> {

    /**
     * 是否存在相同事务
     *
     * @param transactionId
     * @return
     */
    boolean isExistTx(String transactionId);

    // void updateTime();
}
