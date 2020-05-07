package com.topsail.example.consumer;

import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @author Steven
 * @date 2020-05-06
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "topic-send-object", consumerGroup = "MyConsumer2")
public class MyConsumer2 implements RocketMQListener<AccountChangeEvent> {

    @Override
    public void onMessage(AccountChangeEvent accountChangeEvent) {
        log.info("received message: " + accountChangeEvent);
    }

}
