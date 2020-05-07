package com.topsail.reliable.message.core;

/**
 * @author Steven
 * @date 2020-04-29
 */
public class Constants {

    /**
     * 转账主题
     */
    public static final String TOPIC_TRANSFER_ACCOUNT = "TOPIC_TRANSFER_ACCOUNT";

    /**
     * 发送组名
     */
    public static final String PRODUCER_GROUP_BANK1 = "PRODUCER_GROUP_BANK1";

    /**
     * 接受组名
     */
    public static final String CONSUMER_GROUP_BANK2 = "CONSUMER_GROUP_BANK2";

    /**
     * 乐观锁重试次数
     */
    public static final int OPTIMISTIC_LOCK_RETRY_TIMES = 3;

    /**
     * 乐观锁重试间隔时间(毫秒)
     */
    public static final int OPTIMISTIC_LOCK_RETRY_INTERVAL = 500;


}

