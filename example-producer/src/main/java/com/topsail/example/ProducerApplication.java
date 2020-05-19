package com.topsail.example;

import com.topsail.reliable.message.core.MessageDelayLevel;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 * @date 2020-05-06
 */
@Slf4j
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ProducerApplication implements CommandLineRunner {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //testSyncSendString();
        //testSyncSendObject();
        //testAsyncSend();
        //testSendDelay();
        //testSendOneWay();
        testSendWithTag();
        //testSendWithKey();

        /*

        // 发送顺序消息
        rocketMQTemplate.syncSendOrderly("test-topic-4", "I'm order message", "1234");

        */

        System.out.println("send finished!");
    }

    /**
     * 同步发送（消息体为字符串）
     */
    private void testSyncSendString() {
        // 同步发送消息，消息是 String
        String content = "我是一条通过同步机制发送的消息。sendTime@" + LocalDateTime.now();
        SendResult sendResult = rocketMQTemplate.syncSend("topic-test-1", MessageBuilder.withPayload(content).build());
        log.info("== 同步发送字符串 ==");
        log.info("状态:   {}", sendResult.getSendStatus());
        log.info("msgId: {}", sendResult.getMsgId());
    }

    /**
     * 同步发送（消息体为自定义对象）
     */
    private void testSyncSendObject() {
        // 同步发送消息
        AccountChangeEvent accountChangeEvent = AccountChangeEvent.builder()
            .amount(RandomUtils.nextLong(1L, 10L))
            .srcAccountId("src-account-" + RandomStringUtils.randomAlphabetic(8))
            .dstAccountId("dst-account-" + RandomStringUtils.randomAlphabetic(8))
            .build();

        SendResult sendResult = rocketMQTemplate.syncSend("topic-send-object", accountChangeEvent);
        log.info("== 同步发送对象 ==");
        log.info("状态:   {}", sendResult.getSendStatus());
        log.info("msgId: {}", sendResult.getMsgId());
    }

    /**
     * 异步发送
     */
    private void testAsyncSend() {
        // 异步发送消息
        String content = "我是一条通过异步机制发送的消息。sendTime@" + LocalDateTime.now();
        content = StringUtils.rightPad(content, 1024 * 1024 * 4 - 1024, "#");
        rocketMQTemplate.asyncSend("topic-test-1", MessageBuilder.withPayload(content).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("== 异步发送成功 ==");
                log.info("状态:   {}", sendResult.getSendStatus());
                log.info("msgId: {}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.info("== 异步发送失败 ==");
                e.printStackTrace();
            }
        });
    }

    /**
     * 延时消息
     */
    @SneakyThrows
    private void testSendDelay() {

        for (int i = 0; i < 1; i++) {
            String content = "我是一条延迟消息。sendTime@" + LocalDateTime.now();
            String s = StringUtils.rightPad(content, 1024 * 1024 * 4 - 1024, "$");
            System.out.println("s.length: " + s.length());
            SendResult sendResult = rocketMQTemplate.syncSend("topic-test-1", MessageBuilder.withPayload(s).build(), 3000L, MessageDelayLevel.DELAY_10_SECOND);
            log.info("== 发送延时消息 ==");
            log.info("状态:   {}", sendResult.getSendStatus());
            log.info("msgId: {}", sendResult.getMsgId());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * 类似于 UDP，用于强调吞吐量，不强调可靠性的场景
     */
    private void testSendOneWay() {
        String content = "我是一条单向 OneWay 消息。sendTime@" + LocalDateTime.now();
        rocketMQTemplate.sendOneWay("topic-test-1", MessageBuilder.withPayload(content).build());
    }

    /**
     * 发送带 tag 的消息
     * 在发送消息的时候，我们只要把 tags 使用 ":" 添加到 topic 后面就可以了。例如：topicName:tagName
     */
    private void testSendWithTag() {


        String tag1 = "bank1";
        String tag2 = "bank2";

        {
            String content = "我是一条带有 " + tag1 + " 信息的消息。sendTime@" + LocalDateTime.now();
            SendResult sendResult = rocketMQTemplate.syncSend("topic-test-tag:" + tag1, MessageBuilder.withPayload(content).build());
            log.info("== 发送带 tag 的消息 ==");
            log.info("状态:   {}", sendResult);
            log.info("msgId: {}", sendResult.getMsgId());
        }
        {
            String content = "我是一条带有 " + tag2 + " 信息的消息。sendTime@" + LocalDateTime.now();
            SendResult sendResult = rocketMQTemplate.syncSend("topic-test-tag:" + tag2, MessageBuilder.withPayload(content).build());
            log.info("== 发送带 tag 的消息 ==");
            log.info("状态:   {}", sendResult);
            log.info("msgId: {}", sendResult.getMsgId());
        }


    }


    private void testSendWithKey() {
        for (int i = 0; i < 5; i++) {
            String key = UUID.randomUUID().toString();
            String content = "我是一条带有 key=" + key + " 的消息。sendTime@" + LocalDateTime.now();
            SendResult sendResult = rocketMQTemplate.syncSend("topic-test-key", MessageBuilder.withPayload(content).setHeader("KEYS", key).build());
            log.info("== 发送带 key 的消息 ==");
            log.info("状态:   {}", sendResult.getSendStatus());
            log.info("msgId: {}", sendResult.getMsgId());
            log.info("key:   {}", key);
        }
    }

}