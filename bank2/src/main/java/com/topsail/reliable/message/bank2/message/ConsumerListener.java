package com.topsail.reliable.message.bank2.message;

import com.alibaba.fastjson.JSONObject;
import com.topsail.reliable.message.bank2.Bank2Constants;
import com.topsail.reliable.message.bank2.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank2.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2020-02-13
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = Bank2Constants.CONSUMER_GROUP, topic = Bank2Constants.TOPIC)
public class ConsumerListener implements RocketMQListener<String> {

    @Autowired
    AccountService accountInfoService;

    /**
     * 接收消息
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {

        log.info("开始消费消息: {}", message);

        // 解析消息
        JSONObject jsonObject = JSONObject.parseObject(message);
        String accountChangeString = jsonObject.getString("accountChange");

        // 转成AccountChangeEvent
        AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChangeString, AccountChangeEvent.class);

        // 设置账号为李四的
        accountChangeEvent.setAccountNo("2");

        // 更新本地账户，增加金额
        accountInfoService.addAccountInfoBalance(accountChangeEvent);

    }
}
