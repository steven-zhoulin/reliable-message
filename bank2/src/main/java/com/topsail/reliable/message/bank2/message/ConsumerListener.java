package com.topsail.reliable.message.bank2.message;

import com.topsail.reliable.message.bank2.Bank2Constants;
import com.topsail.reliable.message.bank2.entity.event.AccountChangeEvent;
import com.topsail.reliable.message.bank2.service.AccountService;
import com.topsail.reliable.message.bank2.util.JsonUtils;
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
import org.springframework.util.CollectionUtils;

/**
 * @author Steven
 * @date 2020-02-13
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = Bank2Constants.CONSUMER_GROUP_BANK2, topic = Bank2Constants.TOPIC_TRANSFER_ACCOUNT)
public class ConsumerListener implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {

    @Autowired
    AccountService accountInfoService;

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

        consumer.registerMessageListener((MessageListenerConcurrently) (messageExts, context) -> {

            /**
             *
             */
            try {
                if (!CollectionUtils.isEmpty(messageExts)) {
                    messageExts.forEach(messageExt -> {
                        handleMessage(messageExt);
                    });
                }
                log.info("消息消费成功: CONSUME_SUCCESS");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                log.error("消息消费失败: RECONSUME_LATER");
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

        });

    }

    /**
     * 消费消息
     *
     * @param messageExt
     */
    private void handleMessage(MessageExt messageExt) {

        log.info("【消费消息】：第{}次, ext ：{}", messageExt.getReconsumeTimes(), messageExt);

        // 解析消息
        String jsonString = new String(messageExt.getBody());
        AccountChangeEvent accountChangeEvent = JsonUtils.decode(jsonString, AccountChangeEvent.class);

        // 更新本地账户，增加金额
        accountInfoService.addAccountInfoBalance(accountChangeEvent);
    }

}
