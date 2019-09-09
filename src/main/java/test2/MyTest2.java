package test2;

import com.rabbitmq.client.*;
import common.CommonUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用备份交换器来处理未找到队列的消息，
 * 这里需要注意的是消息重新发送到备份交换器的路由键和从生产者发出的路由键是一样的
 * 为了避免路由键的配置，可以将备份交换器的类型设置为fanout，这样就不需要关心备份交换器上绑定的队列的BangingKey了
 * @Author: yyl
 * @Date: 2018/11/23 17:28
 */
public class MyTest2 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    @Test
    public void test1() throws Exception{
        ConnectionFactory factory = CommonUtil.getFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Map<String,Object> map = new HashMap<>(1);
        /**创建一个备份交换器，名称为myAe*/
        map.put("alternate-exchange","myAe");
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,map);
        /**声明一个备份交换器，备份交换器上的类型建议设置为fanout类型*/
        channel.exchangeDeclare("myAe","fanout",true,false,null);
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        /**声明备份交换器上的队列1*/
        channel.queueDeclare("myAe_queue1",true,false,false,null);
        /**声明备份交换器上的队列2*/
        channel.queueDeclare("myAe_queue2",true,false,false,null);
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        /**绑定备份交换器队列1*/
        channel.queueBind("myAe_queue1","myAe","");
        /**绑定备份交换器队列2*/
        channel.queueBind("myAe_queue2","myAe","");
        String message = "hello world";
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY+12,true,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
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
        CommonUtil.getChannel().basicPublish(EXCHANGE_NAME,ROUTING_KEY,true,MessageProperties.PERSISTENT_TEXT_PLAIN,"test".getBytes());
    }
}
