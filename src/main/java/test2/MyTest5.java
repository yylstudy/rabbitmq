package test2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import common.CommonUtil;
import org.junit.Test;

/**
 * rabbitmq消息生产者的确认，确认消息能到达服务器
 * 这里使用了事务的方式，但是事务的方式性能并不理想
 * 这里的到达rabbitmq是指被正确的发往mq的交换器，
 * 如果此交换器上没有匹配的队列，那么消息也会丢失，
 * 所以生成环境中需要配合mandatory参数或者备份交换器一起使用来提高消息传输的可靠性
 * @Author: yyl
 * @Date: 2018/11/24 14:15
 */
public class MyTest5 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    /**
     * 使用事务机制来达到生产者确认的目的
     */
    @Test
    public void test1() throws Exception{
        Channel channel = CommonUtil.getChannel();
        try{
            /**将当前通信信道设置成事务模式*/
            channel.txSelect();
            channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,"transaction message".getBytes());
            /**提交事务*/
            int i= 1/0;
            channel.txCommit();
        }catch (Exception e){
            e.printStackTrace();
            /**消息回滚*/
            channel.txRollback();
        }
    }

    /**
     * 批量发送事务消息，注意这里的提交需要在循环体里面
     */
    @Test
    public void test2() throws Exception{
        Channel channel = CommonUtil.getChannel();
        channel.txSelect();
        for(int i=0;i<10;i++){
            try{
                channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,"transaction message".getBytes());
                channel.txCommit();
            }catch (Exception e){
                e.printStackTrace();
                channel.txRollback();
            }

        }
    }

}
