package com.topsail.reliable.message.core.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author Steven
 * @date 2020-05-19
 */
@Data
@Builder
public class ExecuteStatus {
    private Boolean success;
}
