import lombok.SneakyThrows;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.junit.Test;

/**
 * @author Steven
 * @date 2020-04-12
 */
public class SimpleExampleProduceTest {

    /**
     * 可靠的同步传输被广泛应用于重要通知消息、短信通知、短信营销系统等场景。。
     */
    @Test
    @SneakyThrows
    public void sendMessageSync() {
        DefaultMQProducer producer = new DefaultMQProducer("test-group");
        producer.setNamesrvAddr("10.13.3.13:9876");
        producer.start();
        for (int i = 0; i < 5; i++) {
            Message msg = new Message("TopicTest" , "TagA", ("同步消息:" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        System.out.println("发送完毕！");
        producer.shutdown();
    }

    /**
     * 异步传输通常用于响应时间敏感的业务场景。
     */
    @Test
    @SneakyThrows
    public void sendMessageAsync() {
        DefaultMQProducer producer = new DefaultMQProducer("test-group");
        producer.setNamesrvAddr("10.13.3.13:9876");
        producer.start();
        producer.setRetryTimesWhenSendAsyncFailed(0);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            Message msg = new Message("TopicTest",
                "TagA",
                "OrderID188",
                "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.printf("%-10d SUCCESS %s %n", index, sendResult.getMsgId());
                }
                @Override
                public void onException(Throwable e) {
                    System.out.printf("%-10d Exception %s %n", index, e);e.printStackTrace();
                }
            });
        }
        System.out.println("发送完毕！");
        Thread.sleep(1000 * 1000);
        producer.shutdown();
    }

    /**
     * 单向消息，常用于对可靠性要求不高的场景，比如日志传输。
     */
    @Test
    @SneakyThrows
    public void sendMessageOneWay() {
        DefaultMQProducer producer = new DefaultMQProducer("test-group");
        producer.setNamesrvAddr("10.13.3.13:9876");
        producer.start();
        for (int i = 0; i < 5; i++) {
            Message msg = new Message("TopicTest", "TagA", ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            producer.sendOneway(msg);

        }
        Thread.sleep(1000 * 10);
        producer.shutdown();
    }

}
