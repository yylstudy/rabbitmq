package test2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import common.CommonUtil;
import org.junit.Test;

/**
 * 通过confirm方式来确认消息正确的到达rabbitmq，这里的到达rabbitmq是指被正确的发往mq的交换器，
 * 如果此交换器上没有匹配的队列，那么消息也会丢失，所以生成环境中需要配合mandatory
 * 参数或者备份交换器一起使用来提高消息传输的可靠性
 * 注意confirm有三种方式
 * 1）普通的confirm  这种方式相比事务方式并没有太大的性能提升
 * @Author: yyl
 * @Date: 2018/11/24 14:31
 */
public class MyTest6 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";

    /**
     * 通过客户端确认来实现消息投递的正确性
     * @throws Exception
     */
    @Test
    public void test1() throws Exception{
        Channel channel = CommonUtil.getChannel();
        /**将信道设置成publisher confirm模式*/
        channel.confirmSelect();
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                MessageProperties.PERSISTENT_TEXT_PLAIN," puiblisher confirm message test".getBytes());
        /**客户端未收到确认，则表示消息投递失败，如果channel未被设置成confirmSelect，则调用这个方法抛出异常*/
        if(!channel.waitForConfirms()){
            System.out.println("消息发送失败");
        }
    }

    /**
     * 批量同步confirm
     */
    @Test
    public void test2() throws Exception{
        Channel channel = CommonUtil.getChannel();
        /**将信道设置成publisher confirm模式*/
        channel.confirmSelect();
        for(int i=0;i<10;i++){
            channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                    MessageProperties.PERSISTENT_TEXT_PLAIN," puiblisher confirm message test".getBytes());
            if(!channel.waitForConfirms()){
                System.out.println("消息发送失败");
            }
        }
    }
}
