package test2;

import com.rabbitmq.client.*;
import common.CommonUtil;
import javafx.util.Builder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq过期时间设置，一旦队列上的消息的生存时间超过过期时间，那么消息就会变成死信
 * @Author: yyl
 * @Date: 2018/11/23 17:28
 */
public class MyTest3 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    @Test
    public void test1() throws Exception{
        ConnectionFactory factory = CommonUtil.getFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,null);
        Map<String,Object> param = new HashMap<>();
        /**设置队列上消息的过期时间*/
        param.put("x-message-ttl",6000);
        /**设置队列的过期时间*/
        param.put("x-expires",180000);
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        String message = "hello world";
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,true,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        /**注意这里需要sleep*/
        Thread.sleep(100);
        channel.close();
        connection.close();
    }

    /**
     * 发送消息
     * @throws Exception
     */
    @Test
    public void test2() throws  Exception{
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        /**持久化消息*/
        builder.deliveryMode(2);
        /**设置消息的过期时间*/
        builder.expiration("5000");
        AMQP.BasicProperties properties = builder.build();
        /**单独为每条消息设置过期时间*/
        CommonUtil.getChannel().basicPublish(EXCHANGE_NAME,ROUTING_KEY,true,properties,"test".getBytes());
    }
}
