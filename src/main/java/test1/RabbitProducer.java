package test1;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import common.CommonUtil;

/**
 * rabbitmq HelloWorld测试
 * @Author: yyl
 * @Date: 2018/11/22 18:36
 */
public class RabbitProducer {
    private static final String EXCHANGE_NAME = "exchange_demo";
    private static final String ROUTING_KEY="routingkey-demo";
    private static final String QUEUE_NAME = "queue_demo";
    private static final String IP_ADDRESS = "192.168.111.128";
    private static final int port = 5672;
    public static void main(String[] args) throws Exception{
        /**创建一个RabbitMq工厂*/
        ConnectionFactory factory = CommonUtil.getFactory();
        /**新建一个连接*/
        Connection connection = factory.newConnection();
        /**创建一个信道*/
        Channel channel = connection.createChannel();
        /**创建一个direct、持久化的、非自动删除的交换器
         * exchange :  交换器的名称
         * type:  交换器类型有四种
         * 1）fanout：它会把所有发送到该交换器的消息路由到所有与该交换器绑定的队列中,无视routingKey,这个就是广播类型的
         * 2）direct：它会把消息路由到那些BingingKey和RoutingKey完全匹配的队列中
         * 3）topic：RoutingKey和BingingKey之间作模糊匹配 #.*.*  #用于匹配一个单词，*用于匹配多个单词 也可以是0个
         * 4)headers:不依赖于路由键的匹配规则来路由消息，而是根据发送消息中的headers属性进行匹配 基本不用
         * durable:  是否持久化  持久化可以将交换器存盘，在服务器重启的时候不会丢失相关的信息
         * autoDelete: 设置是否自动删除，自动删除的前提是至少有一个队列或者交换器与这个交换器绑定，之后所有与这个交换器绑定的
         * 队列或者交换器都与此解绑
         * internal:设置是否内置，默认为false,如果为true，表示是内置的交换器，客户端程序无法直接发送消息到这个交换器中
         * 只能通过交换器路由到交换器的这种方式
         * arguments:  其他一些结构化参数
         * */
        //生产者和消费者都可以声明一个队列或者交换器，如果尝试声明一个已经存在的交换器或者队列，只要声明的参数完全匹配现存的交换器
        //或者队列，rabbitmq就什么也不做，并返回成功，声明参数不匹配则抛出异常
        channel.exchangeDeclare(EXCHANGE_NAME,"direct",true,false,false,null);
        //声明交换器无需等待服务器返回
//        channel.exchangeDeclareNoWait(EXCHANGE_NAME,"direct",true,false,false,null);
        /**这个方法是检测相应的交换器是否存在，如果存在则正常返回，如果不存在则抛出异常*/
        channel.exchangeDeclarePassive(EXCHANGE_NAME);
        /**创建一个持久化的、非排他的、非自动删除的队列
         * queue ：队列的名称
         * durable：是否持久化
         * exlusive:是否排他 ，如果一个队列被声明为排他队列，该队列仅对首次声明它的连接可见，并在连接断开时自动删除，
         * 这里需要注意三点：排它队列是基于Connection可见的，同一个连接的不同信道可以同时访问同一Connection创建的排他队列
         * arguments ：设置队列的其它一些参数 x-message-ttl,x-expires,x-max-length,x-max-length-bytes,
         * x-dead-letter-exchange,x-dead-letter-routing-key,x-max-priority等
         * */
        channel.queueDeclare(QUEUE_NAME,true,false,false,null);
        /**这个方法是检测相应的队列是否存在，如果存在则正常返回，如果不存在则抛出异常*/
        channel.queueDeclarePassive(QUEUE_NAME);
        /**将交换器和队列通过路由键绑定*/
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        //清空队列中的数据，而不是删除队列本身
//        channel.queuePurge(QUEUE_NAME);
        /**将交换器和交换器绑定，这样就是生产者将消息发送至交换器中，交换器根据路由键找到与其匹配的另一个交换器再由那个交换器发送至
         * 相应的队列*/
//        channel.exchangeBind();
        /**将交换器和队列解绑*/
//        channel.queueUnbind()
        MyBean myBean = new MyBean("yyl", "29", "man");
        /**发送一条持久化的消息
         * exchange:交换器名称
         * routingKey:路由键
         * MessageProperties 消息属性，如消息是否持久化，过期时间等等
         * */
        channel.basicPublish(EXCHANGE_NAME,ROUTING_KEY,
                MessageProperties.PERSISTENT_TEXT_PLAIN, JSON.toJSONBytes(myBean));
        channel.close();
        connection.close();
    }

}
