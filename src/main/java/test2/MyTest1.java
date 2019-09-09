package test2;

import com.rabbitmq.client.*;
import common.CommonUtil;

import java.io.IOException;

/**
 * 使用channel.addReturnListener()方法来处理mandatory为true的未找到队列的消息，
 * 不过这样生产者的代码有点麻烦，另一种方式是采用备份交换器来实现 MyTest2
 * @Author: yyl
 * @Date: 2018/11/22 18:36
 */
public class MyTest1 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    public static void main(String[] args) throws Exception{
        Channel channel = CommonUtil.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,false,null);
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        String message = "hello world";
        /**发送一个没有路由键的消息
         * mandatory: 这个参数设置为true，当交换器无法根据路由键找到队列时，此时rabbitmq会调用Basic.Return将消息返回给
         * 生产者，当设置为false时，rabbitmq直接丢弃消息，生产者通过channel.addReturnListener来处理返回的消息
         * immediate: 这个为true时，如果交换器在将消息路由到队列时，发现队列上没有任何消费者，那么这条消息将不会被存入队列，并返回
         * */
        channel.basicPublish(EXCHANGE_NAME,"routingkey-demo1",true,false,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        channel.addReturnListener(new ReturnListener() {
            @Override
            public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("消息未找到路由键，被返回"+msg);
            }
        });
        //注意这里需要sleep
        Thread.sleep(1000);
        CommonUtil.close();
    }

}
