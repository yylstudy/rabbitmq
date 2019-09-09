package test3;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import test1.MyBean;

/**
 * 逻辑队列发送消息
 * @Author yang.yonglian
 * @ClassName: test3
 * @Description: TODO(这里描述)
 * @Date 2019/8/9 0009
 */
public class MyTest1 {
    public static void main(String[] args) throws Exception{
        MultipleQueue multipleQueue = new MultipleQueue(4);
        Connection connection = MultipleQueue.getConnection();
        Channel channel = connection.createChannel();
        multipleQueue.exchangeDeclare(channel, "exchange", "direct", true, false, null);
        multipleQueue.queueDeclare(channel, "queue", true, false, true, null);
        multipleQueue.queueBind(channel, "queue", "exchange", "rk",null);
        MyBean myBean = new MyBean("yyl2","30","man");
        Message message = new Message(1, JSON.toJSONString(myBean));
        multipleQueue.basicPublish(channel, "exchange", "rk",
                false, MessageProperties.PERSISTENT_TEXT_PLAIN,JSON.toJSONBytes(message));

        MultipleQueue.closeConnection();
    }
}
