package com.topsail.demo1;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Steven
 * @date 2020-04-28
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer("group-demo1-producer");
        defaultMQProducer.setNamesrvAddr("10.13.3.13:9876");
        defaultMQProducer.start();
        System.out.println("启动生产者");

        Message message = new Message("TopicTest", "tag-1", "我是延迟消息".getBytes());
        message.setDelayTimeLevel(4);
        System.out.println(LocalDateTime.now().plusSeconds(30) + " 收到消息！");
        SendResult send = defaultMQProducer.send(message);

    }

}
