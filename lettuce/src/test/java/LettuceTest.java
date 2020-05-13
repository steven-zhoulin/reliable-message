/**
 * @author Steven
 * @date 2020-05-11
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;

/**
 * @author Levin
 * @since 2018/5/10 0010
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class LettuceTest {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;


    @Test
    public void get() {
        redisTemplate.opsForValue().set("k1", "v1");
        System.out.println("set ok!");

    }
}