package com.topsail.reliable.message.core;

/**
 * @author Steven
 * @date 2020-05-07
 */
public interface MessageDelayLevel {

    /**
     * 对应延时消息的 18 级单位
     */
    int DELAY_1_SECOND = 1;
    int DELAY_5_SECOND = 2;
    int DELAY_10_SECOND = 3;
    int DELAY_30_SECOND = 4;
    int DELAY_1_MINUTE = 5;
    int DELAY_2_MINUTE = 6;
    int DELAY_3_MINUTE = 7;
    int DELAY_4_MINUTE = 8;
    int DELAY_5_MINUTE = 9;
    int DELAY_6_MINUTE = 10;
    int DELAY_7_MINUTE = 11;
    int DELAY_8_MINUTE = 12;
    int DELAY_9_MINUTE = 13;
    int DELAY_10_MINUTE = 14;
    int DELAY_20_MINUTE = 15;
    int DELAY_30_MINUTE = 16;
    int DELAY_1_HOUR = 17;
    int DELAY_2_HOUR = 18;

}
