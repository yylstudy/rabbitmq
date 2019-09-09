package test3;

/**
 * @Author yang.yonglian
 * @ClassName: test3
 * @Description: TODO(这里描述)
 * @Date 2019/8/9 0009
 */
public interface IMsgCallback {
    ConsumeStatus consumeMsg(Message message);
}
