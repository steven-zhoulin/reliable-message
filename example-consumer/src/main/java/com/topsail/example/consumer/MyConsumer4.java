package com.topsail.example.consumer;

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
@RocketMQMessageListener(topic = "topic-test-key", consumerGroup = "MyConsumer4")
public class MyConsumer4 implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("received message: " + message);
    }

}
