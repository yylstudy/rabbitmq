package common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @Author: yyl
 * @Date: 2018/11/23 16:38
 */
public class CommonUtil {
    private static ConnectionFactory factory = null;
    public static ConnectionFactory getFactory(){
        if(factory==null){
            factory =new ConnectionFactory();
            factory.setPort(5672);
            factory.setHost("192.168.220.128");
            factory.setPassword("root");
            factory.setUsername("root");
        }
        return factory;
    }
    public static Channel getChannel(){
        try{
            Connection connection = getFactory().newConnection();
            return connection.createChannel();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
