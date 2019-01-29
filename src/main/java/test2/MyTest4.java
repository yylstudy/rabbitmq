package test2;

import com.rabbitmq.client.*;
import common.CommonUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq死信队列，当消息变成死信后，就会进入死信交换器，死信交换器上绑定的队列就是死信队列
 * rabbitmq延迟队列，rabbitmq上并不直接支持延迟队列，但是可以使用过期时间和死信队列实现延迟队列
 * 方法就是消费者并不直接订阅消息发送的队列，而是订阅消息的死信队列，这样就可以延迟获取消息了
 * @Author: yyl
 * @Date: 2018/11/23 17:28
 */
public class MyTest4 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    @Test
    public void test1() throws Exception{
        ConnectionFactory factory = CommonUtil.getFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,null);
        /**声明一个死信交换器*/
        channel.exchangeDeclare("dlx_exchange","direct",true,false,null);
        Map<String,Object> param = new HashMap<>();
        /**设置消息的过期时间，等待其变成死信*/
        param.put("x-message-ttl",6000);
        /**设置队列上绑定的死信交换器*/
        param.put("x-dead-letter-exchange","dlx_exchange");
        /**指定死信的路由键*/
        param.put("x-dead-letter-routing-key","dlx_routingkey");
        channel.queueDeclare(QUEUE_NAME,true,false,false,param);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        /**声明一个死信队列*/
        channel.queueDeclare("dlx_queue",true,false,false,null);
        /**将死信队列和死信交换器绑定*/
        channel.queueBind("dlx_queue","dlx_exchange","dlx_routingkey");
        String message = "hello world";
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,true,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
        /**注意这里需要sleep*/
        Thread.sleep(100);
        channel.close();
        connection.close();
    }

}
