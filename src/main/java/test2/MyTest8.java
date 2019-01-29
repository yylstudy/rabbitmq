package test2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;
import common.CommonUtil;
import org.junit.Test;
import sun.misc.Cleaner;

import java.io.IOException;
import java.io.WriteAbortedException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 客户端确认publisher confirm 批量异步确认
 * 同步的缺点是性能较事务方式没有太大提升
 * 但是异步确认的方式能极大提升性能，单位问题在于如果出现未被确认的情况下，需要将这一批次的消息重发
 * 因为异步确认此事并不知道哪个消息未被确认，所以如果消息经常丢失，异步提交的方式的性能是不升反降的
 * @Author: yyl
 * @Date: 2018/11/24 15:08
 */
public class MyTest8 {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    /**存放未确认消息的标识tag*/
    private  SortedSet<Long> confirmSet = new TreeSet<>();
    @Test
    public void test1() throws Exception {
        Channel channel = CommonUtil.getChannel();
        channel.confirmSelect();
        /**添加一个消息确认的监听器*/
        channel.addConfirmListener(new ConfirmListener() {
            /**
             * 处理收到确认消息
             * @param deliveryTag 确认消息的流水号（如果是多条，这个就是确认的最后一条消息的流水号）
             * @param multiple 是否是多个
             * @throws IOException
             */
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("消息被确认,seqNo:"+deliveryTag+" multiple:"+multiple);
                try{
                    /**多条就删除这个流水号之前的*/
                    if(multiple){
                        /**这里可能会抛出ConcurrentModificationException异常 暂时不管 */
                        confirmSet.headSet(deliveryTag+1).clear();
                    }else{
                        confirmSet.remove(deliveryTag);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            /**
             * 处理未收到确认的消息
             * @param deliveryTag 未被确认的消息的流水号
             * @param multiple 是否是多个
             * @throws IOException
             */
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("消息未被确认,seqNo:"+deliveryTag+" multiple:"+multiple);
            }
        });
        for(int i=0;i<1000;i++){
            /**获取下一次发送消息的流水号*/
            long nextSeqNo = channel.getNextPublishSeqNo();
            channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,("test message"+i).getBytes());
            /**流水号进缓存*/
            confirmSet.add(nextSeqNo);
        }
        Thread.sleep(5000);
    }
}
