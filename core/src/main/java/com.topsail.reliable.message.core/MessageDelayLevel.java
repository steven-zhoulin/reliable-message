package com.topsail.reliable.message.core;

/**
 * @author Steven
 * @date 2020-05-07
 */
public final class MessageDelayLevel {

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

    private static String[] delayNames = {
        "",
        " 1 second later",
        " 5 seconds later",
        "10 seconds later",
        "30 seconds later",
        " 1 minute later",
        " 2 minutes later",
        " 3 minutes later",
        " 4 minutes later",
        " 5 minutes later",
        " 6 minutes later",
        " 7 minutes later",
        " 8 minutes later",
        " 9 minutes later",
        "10 minutes later",
        "20 minutes later",
        "30 minutes later",
        " 1 hour later",
        " 2 hours later"
    };

    public static String display(int delayLevel) {
        return delayNames[delayLevel];
    }

}
