package test3;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author yang.yonglian
 * @ClassName: test3
 * @Description: TODO(这里描述)
 * @Date 2019/8/9 0009
 */
@Getter
@Setter
public class Message implements Serializable {
    private long msgSeq;
    private String msgBody;
    private long deliveryTag;
    public Message(long msgSeq,String msgBody){
        this.msgSeq = msgSeq;
        this.msgBody = msgBody;
    }

}
