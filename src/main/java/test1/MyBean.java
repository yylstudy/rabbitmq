package test1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author yang.yonglian
 * @ClassName: test1
 * @Description: TODO(这里描述)
 * @Date 2019/8/6 0006
 */
@Getter
@Setter
@AllArgsConstructor
public class MyBean implements Serializable {
    private String name;
    private String age;
    private String sex;
}
