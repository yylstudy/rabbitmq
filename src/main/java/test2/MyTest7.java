package test2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import common.CommonUtil;
import org.junit.Test;

/**
 * 2）批量confirm  这种方式的缺点是：如果有一个消息出现nack或者超时的情况下，这一批消息都要重发
 *    这会带来明显的重复消息数量
 * @Author: yyl
 * @Date: 2018/11/24 15:29
 */
public class MyTest7 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    @Test
    public void test1() throws Exception{
        Channel channel = CommonUtil.getChannel();
        channel.confirmSelect();
        for(int i=0;i<100;i++){
            channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,MessageProperties.PERSISTENT_TEXT_PLAIN,"test".getBytes());
        }
        try{
            /**批量发送一次确认*/
            if(!channel.waitForConfirms()){
                System.out.println("该批次消息中有发送失败的情况");
            }
        }catch (Exception e){
            System.out.println("发送失败");
        }
    }
}
