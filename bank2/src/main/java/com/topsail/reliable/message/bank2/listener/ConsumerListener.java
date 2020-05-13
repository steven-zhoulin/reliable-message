package com.topsail.reliable.message.bank2.listener;

import com.topsail.reliable.message.bank2.service.AccountService;
import com.topsail.reliable.message.core.Constants;
import com.topsail.reliable.message.core.convert.MessageConvert;
import com.topsail.reliable.message.core.entity.event.AccountChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 此消费者,只关注 bank1 给 bank2 发出的账户资金变动消息
 *
 * @author Steven
 * @date 2020-02-13
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = Constants.CONSUMER_GROUP_BANK2, topic = Constants.TOPIC_BANK1_ACCOUNT_CHANGE, selectorExpression = "bank2")
public class ConsumerListener implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    private AccountService accountInfoService;

    /**
     * 该函数必须保留，但不会被调用，出于兼容性考虑
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        log.info("实现 RocketMQPushConsumerLifecycleListener 监听器之后，此方法不调用");
    }

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {

        consumer.setConsumeMessageBatchMaxSize(1);
        consumer.registerMessageListener((MessageListenerConcurrently) (messageExts, context) -> {
            Assert.isTrue(messageExts.size() == 1, "ConsumeMessageBatchMaxSize != 1, curr = " + messageExts.size());

            // 消费消息
            MessageExt messageExt = messageExts.get(0);
            String key = messageExt.getKeys();
            String topic = messageExt.getTopic();
            int reconsumeTimes = messageExt.getReconsumeTimes();

            // 解析消息
            AccountChangeEvent accountChangeEvent = MessageConvert.from(messageExt);

            try {
                // 更新本地账户，增加金额
                accountInfoService.addAccountInfoBalance(accountChangeEvent);
                log.info("消费成功[第{}次重试], topic: {} key: {}", reconsumeTimes, topic, key);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("消费失败[第{}次重试], topic: {}, key: {}", reconsumeTimes, topic, key);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }


        });
    }
}
