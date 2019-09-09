package test3;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * 拉模式的消费
 * @Author yang.yonglian
 * @ClassName: test3
 * @Description: TODO(这里描述)
 * @Date 2019/8/9 0009
 */
public class MyTest2 {
    public static void main(String[] args) throws Exception{
        MultipleQueue multipleQueue = new MultipleQueue(4);
        Connection connection = MultipleQueue.getConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(64);
        multipleQueue.basicConsume(channel, "queue", false,
                "consumer_yyl", multipleQueue.getBlockingQueue(), new IMsgCallback() {
                    @Override
                    public ConsumeStatus consumeMsg(Message message) {
                        ConsumeStatus status = ConsumeStatus.FAIL;
                        if(message!=null){
                            status = ConsumeStatus.SUCCESS;
                        }
                        return status;
                    }
                });
    }
}
