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
        //设置客户端最多接收未被ack的消息的个数，这个参数主要是为了多消费者情况下，某些消费者未及时处理完成消息
        //当消费者中未ack的消息达到这个上限值时，rabbitmq就不会向这个消费者发送任何消息，直到
        //消费者中的未ack的消息数减少
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
                //手动确认ack，否则消息还是在队列上
                //envelope.getDeliveryTag()表示消息的编号是个64位的长整形
                channel.basicAck(envelope.getDeliveryTag(),false);
                //拒绝消息，requeue如果为true，rabbitmq就将这条消息重新存入队列，否则rabbitmq直接将消息删除，而不会把他发送给
                //新的消费者
                //channel.basicReject(envelope.getDeliveryTag(),false);
                //批量拒绝消息，如果multiple为false，那么basicNack方法和basicReject方法是一样的
                //如果multiple为true，那么则表示拒绝了当前消息的deliveryTag编号之前的所有未被消费者确认的消息
                //channel.basicNack(envelope.getDeliveryTag(),true,false);
            }
        };
        boolean autoAck = false;
        /**推模式消费这个队列上的消息
         * queue:队列的名称
         * autoAck：建议设置成false，即不自动确认
         * consumerTag:消费者标签，用来区分多个消费者
         * callback:设置消费者的回调函数
         * */
        channel.basicConsume(QUEUE_NAME,autoAck,consumer);
        //拉模式消费这个队列上的消息
        //channel.basicGet(QUEUE_NAME,autoAck);
        //等待回调方法执行完毕关闭连接，这里应该是异步的
        //Thread.sleep(1000);
        //channel.close();
        //connection.close();
    }
    
}
