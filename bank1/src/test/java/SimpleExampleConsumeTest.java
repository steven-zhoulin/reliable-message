import lombok.SneakyThrows;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Test;

import java.util.List;

/**
 * @author Steven
 * @date 2020-04-12
 */
public class SimpleExampleConsumeTest {

    @Test
    @SneakyThrows
    public void consume() {

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test-group2");
        consumer.setNamesrvAddr("10.13.3.13:9876");
        consumer.subscribe("TopicTest", "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    System.out.printf("%s Receive new message: %s %n", Thread.currentThread().getName(), String.valueOf(msg.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //Launch the consumer instance.
        consumer.start();
        Thread.sleep(1000 * 1000);
        System.out.printf("Consumer Started.%n");
    }

}
