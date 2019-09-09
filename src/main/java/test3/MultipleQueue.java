package test3;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 打破单队列的性能瓶颈，封装的一个逻辑队列，内部包含多个实际队列
 * @Author: yyl
 * @Date: 2018/11/24 16:44
 */
public class MultipleQueue {
    private static String host = "192.168.111.128";
    private static int port = 5672;
    private static String vhost = "/";
    private static String username = "root";
    private static String password = "root";
    private static Connection connection;
    /**分片数，表示一个逻辑队列背后的实际队列数*/
    private int subdivisionNum;

    private ConcurrentLinkedDeque blockingQueue;

    public MultipleQueue(int subdivisionNum){
        this.subdivisionNum = subdivisionNum;
        blockingQueue = new ConcurrentLinkedDeque();
    }

    /**
     * 获取一个链接
     * @return
     */
    public static Connection getConnection(){
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setPassword(password);
            factory.setUsername(username);
            factory.setVirtualHost(vhost);
            connection = factory.newConnection();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * 关闭一个链接
     */
    public static void closeConnection(){
        try{
            if(connection!=null){
                connection.close();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 声明交换器
     * @param channel
     * @param exchange
     * @param type
     * @param durable
     * @param autoDelete
     * @param args
     */
    public  void exchangeDeclare(Channel channel, String exchange, String type, boolean durable,
                                       boolean autoDelete, Map<String,Object> args){
        try{
            channel.exchangeDeclare(exchange, type, durable, autoDelete, args);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 声明队列
     * @param channel
     * @param queue
     * @param durable
     * @param exclusive
     * @param autoDelete
     * @param args
     */
    public  void queueDeclare(Channel channel,String queue,boolean durable,boolean exclusive,boolean autoDelete,Map<String,Object> args){
        try{
            for(int i=0;i<subdivisionNum;i++){
                channel.queueDeclare(queue+"_"+i, durable, exclusive, autoDelete, args);
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 队列绑定
     * @param channel
     * @param queue
     * @param exchange
     * @param routingKey
     * @param args
     */
    public void queueBind(Channel channel,String queue,String exchange,String routingKey,Map<String,Object> args){
        try{
            for(int i=0;i<subdivisionNum;i++){
                channel.queueBind(queue+"_"+i, exchange, routingKey+"_"+i,args);
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送消息
     * @param channel
     * @param exchange
     * @param routingKey
     * @param mandatory
     * @param pros
     * @param body
     */
    public void basicPublish(Channel channel, String exchange, String routingKey,
                             boolean mandatory, AMQP.BasicProperties pros,byte[] body){
        try{
            Random random = new Random();
            int index = random.nextInt(subdivisionNum);
            String rk = routingKey+"_"+index;
            channel.basicPublish(exchange, rk,mandatory, pros, body);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 拉模式队列中的消息
     * @param channel
     * @param queue
     * @param autoAck
     * @return
     */
    public GetResponse basicGet(Channel channel,String queue,boolean autoAck ){
        try{
            Random random = new Random();
            int index = random.nextInt(subdivisionNum);
            //先随机拉取一个队列中的消息
            GetResponse getResponse = channel.basicGet(queue+"_"+index,autoAck);
            if(getResponse==null){
                //未获取到，则遍历所有的队列进行消息拉取
                for(int i=0;i<subdivisionNum;i++){
                    getResponse = channel.basicGet(queue+"_"+i,autoAck);
                    if(getResponse!=null){
                        return getResponse;
                    }
                }
            }
            return getResponse;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 推模式获取队列中的消息
     * @param channel
     * @param queue
     * @param autoAck
     * @param consumerTag
     * @param newBlockingQueue
     * @param msgCallback
     */
    public void basicConsume(Channel channel,String queue,boolean autoAck,String consumerTag,
                             ConcurrentLinkedDeque<Message> newBlockingQueue,IMsgCallback msgCallback){
        try{
            startConsume(channel,queue,autoAck,consumerTag,newBlockingQueue);
            while(true){
                Message message = newBlockingQueue.peekFirst();
                if(message!=null){
                    ConsumeStatus consumeStatus = msgCallback.consumeMsg(message);
                    newBlockingQueue.removeFirst();
                    if(consumeStatus==ConsumeStatus.SUCCESS){
                        channel.basicAck(message.getDeliveryTag(), false);
                    }else{
                        channel.basicReject(message.getDeliveryTag(), false);
                    }
                }else{
                    Thread.sleep(100);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 遍历逻辑队列中的所有队列，推模式获取消息并放入queue
     * @param channel
     * @param queue
     * @param autoAck
     * @param consumerTag
     * @param newBlockingQueue
     */
    private void startConsume(Channel channel,String queue,boolean autoAck,String consumerTag,
                              ConcurrentLinkedDeque<Message> newBlockingQueue){
        try{
            for(int i=0;i<subdivisionNum;i++){
                String queueName = queue+"_"+i;
                channel.basicConsume(queueName, autoAck,consumerTag, new NewConsumer(channel,newBlockingQueue));
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static class NewConsumer extends DefaultConsumer{
        private ConcurrentLinkedDeque newBlockingQueue;

        public NewConsumer(Channel channel,ConcurrentLinkedDeque<Message> newBlockingQueue){
            super(channel);
            this.newBlockingQueue = newBlockingQueue;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
            Message message = JSON.parseObject(body, Message.class);
            message.setDeliveryTag(envelope.getDeliveryTag());
            newBlockingQueue.addLast(message);
        }
    }

    public ConcurrentLinkedDeque getBlockingQueue() {
        return blockingQueue;
    }
}
