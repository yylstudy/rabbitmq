package test1;

import com.rabbitmq.client.*;
import common.CommonUtil;

import java.io.IOException;

/**
 * 消息消费者
 * @Author: yyl
 * @Date: 2018/11/22 19:41
 */
public class RabbitConsumer {
    private static String QUEUE_NAME = "queue_demo";
    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = CommonUtil.getFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        /**设置客户端最多接收未被ack的消息的个数*/
        channel.basicQos(64);
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("接收到消息："+new String(body));
                try{
                    Thread.sleep(1);
                }catch (Exception e){
                    e.printStackTrace();
                }
                /**手动确认ack，否则消息还是在队列上*/
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        boolean autoAck = false;
        /**消费这个队列上的消息
         * queue:队列的名称
         * autoAck：建议设置成false，即不自动确认
         * callback:设置消费者的回调函数
         * */
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);
        /**等待回调方法执行完毕关闭连接，这里应该是异步的*/
        Thread.sleep(10);
        channel.close();
        connection.close();
    }
    
}
