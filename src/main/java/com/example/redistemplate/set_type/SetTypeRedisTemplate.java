package com.example.redistemplate.set_type;

import com.example.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhangdj
 * @Date 2021/5/20:16:44
 * @Description redis中操作无序集合类型【set】的数据
 * Redis的Set是string类型的无序集合。集合成员是唯一的，这就意味着集合中不能出现重复的数据。
 * Redis 中 集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是O(1)。
 * public interface SetOperations<K,V>
 * SetOperations提供了对无序集合的一系列操作：
 */
@RestController
@RequestMapping("/settest")
public class SetTypeRedisTemplate {

    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    RedisTemplate<String, Student> redisTemplateStudent;

    /**
     * 操作redis中的字符串类型的数据的三种方式
     * add(K key, V value1,V value1...)新增一个无序集合类型的值,key是键，value是值。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:40
     */
    @RequestMapping("/findvalue")
    public void redisTemplateTest() {
        //1、通过redisTemplate设置值
        redisTemplate.boundSetOps("setKey").add("setValue1", "setValue2", "setValue3");

        //2、通过BoundValueOperations设置值
        BoundSetOperations setKey = redisTemplate.boundSetOps("setKey");
        setKey.add("setValue1", "setValue2", "setValue3");

        //3、通过ValueOperations设置值
        SetOperations setOps = redisTemplate.opsForSet();
        setOps.add("setKey", "SetValue1", "setValue2", "setValue3");
    }

    /**
     * isMember(Object key) 判断指定的key是否存在
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 17:08
     */
    public void redisIfThereIsA() {
        Boolean isEmpty = redisTemplate.boundSetOps("setKey").isMember("setValue2");
        System.out.println("判断是否存在：" + isEmpty);
    }

    /**
     * 从redis中获取无序集合类型缓存的值
     * members(K key)获取key键对应的值。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:52
     */
    @RequestMapping("/get")
    public void redisGet() {
        //1、通过redisTemplate获取值
        Set set1 = redisTemplate.boundSetOps("setKey").members();

        //2、通过BoundValueOperations获取值
        BoundSetOperations setKey = redisTemplate.boundSetOps("setKey");
        Set set2 = setKey.members();

        //3、通过ValueOperations获取值
        SetOperations setOps = redisTemplate.opsForSet();
        Set set3 = setOps.members("setKey");
    }

    /**
     * size(K key)获取指定无序集合的长度。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:14
     */
    public void redisSize() {
        Long size = redisTemplate.boundSetOps("setKey").size();
        Long stringValueLength = redisTemplate.opsForSet().size("setKey");
        System.out.println("通过size(K key)方法获取字符串的长度:" + stringValueLength);
    }

    /**
     * 设置过期时间的两种方式
     * expire()用来设置时间
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:46
     */
    public void redisExpire() {
        // 单独对key设置过期时间
        redisTemplate.boundSetOps("setKey").expire(1, TimeUnit.MINUTES);
        redisTemplate.expire("setKey", 1, TimeUnit.MINUTES);
    }

    /**
     * 从redis中删除数据
     * remove(Object... var1) 移除指定的元素
     * delete() 删除key的value
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:00
     */
    public void redisDelete() {
        Long result1 = redisTemplate.boundSetOps("setKey").remove("setValue1");
        Boolean result = redisTemplate.delete("setKey");
    }
}