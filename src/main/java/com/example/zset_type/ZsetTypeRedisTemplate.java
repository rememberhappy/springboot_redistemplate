package com.example.zset_type;

import com.example.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhangdj
 * @Date 2021/5/20:17:20
 * @Description redis中操作有序集合类型【zset】的数据
 * Redis 有序集合和无序集合一样也是string类型元素的集合,且不允许重复的成员。
 * 不同的是每个元素都会关联一个double类型的分数。redis正是通过分数来为集合中的成员进行从小到大的排序。
 * 有序集合的成员是唯一的,但分数(score)却可以重复。
 * public interface ZSetOperations<K,V>
 */
public class ZsetTypeRedisTemplate {

    @Autowired
    RedisTemplate redisTemplate;

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
    public void redisTemplateTest() {//1、通过redisTemplate设置值
        redisTemplate.boundZSetOps("zSetKey").add("zSetVaule", 100D);

        //2、通过BoundValueOperations设置值
        BoundZSetOperations zSetKey = redisTemplate.boundZSetOps("zSetKey");
        zSetKey.add("zSetVaule", 100D);

        //3、通过ValueOperations设置值
        ZSetOperations zSetOps = redisTemplate.opsForZSet();
        zSetOps.add("zSetKey", "zSetVaule", 100D);
    }

    /**
     * 从redis中获取String类型缓存的值
     * get(Object key)获取key键对应的值。
     * get(K key, long start, long end)截取key键对应值得字符串，从开始下标位置开始到结束下标的位置(包含结束下标)的字符串。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:52
     */
    @RequestMapping("/get")
    public void redisGet() {
        //1、通过redisTemplate设置值
        String str1 = (String) redisTemplate.boundValueOps("StringKey1").get();

        //2、通过BoundValueOperations获取值
        BoundValueOperations stringKey = redisTemplate.boundValueOps("StringKey");
        String str2 = (String) stringKey.get();

        //3、通过ValueOperations获取值
        ValueOperations ops = redisTemplate.opsForValue();
        String str3 = (String) ops.get("StringKey");
    }

    /**
     * setIfAbsent(K key, V value) 如果键不存在则新增,存在则不改变已经有的值。
     * 返回true，说明键不存在，进行新增数据
     * 返回false，说明键存在，不改变原有的值
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 13:49
     */
    public void redisSetIfAbsent() {
        boolean absentBoolean = redisTemplate.opsForValue().setIfAbsent("absentValue", "fff");
        System.out.println("通过setIfAbsent(K key, V value)方法判断变量值absentValue是否不存在:" + absentBoolean);
        if (absentBoolean) {
            String absentValue = redisTemplate.opsForValue().get("absentValue") + "";
            System.out.print(",不存在，则新增后的值是:" + absentValue);
            boolean existBoolean = redisTemplate.opsForValue().setIfAbsent("absentValue", "eee");
            System.out.print(",再次调用setIfAbsent(K key, V value)判断absentValue是否不存在并重新赋值:" + existBoolean);
            if (!existBoolean) {
                absentValue = redisTemplate.opsForValue().get("absentValue") + "";
                System.out.print("如果存在,则重新赋值后的absentValue变量的值是:" + absentValue);
            }
        }
    }

    /**
     * 重新给key赋值
     * getAndSet(K key, V value)获取原来key键对应的值并重新赋新值。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:08
     */
    public void redisGetAndSet() {
        String oldAndNewStringValue = (String) redisTemplate.opsForValue().getAndSet("stringValue1", "ccc");
        System.out.print("通过getAndSet(K key, V value)方法获取原来的" + oldAndNewStringValue + ",");
        String newStringValue = (String) redisTemplate.opsForValue().get("stringValue");
        System.out.println("修改过后的值:" + newStringValue);
    }

    /**
     * 在原有的值基础上新增字符串到末尾。
     * append(K key, String value)在原有的值基础上新增字符串到末尾，返回追加后值得总长度
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:02
     */
    @RequestMapping("/append")
    public void redisAppend() {
        Integer append1 = redisTemplate.opsForValue().append("StringKey1", "append");
    }

    /**
     * size(K key)获取指定字符串的长度。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:14
     */
    public void redisSize() {
        Long size = redisTemplate.boundValueOps("stringValue").size();
        Long stringValueLength = redisTemplate.opsForValue().size("stringValue");
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
        // 在创建的时候设置过期时间
        redisTemplate.boundValueOps("StringKey").set("StringValue", 1, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("StringValue", "StringVaule", 1, TimeUnit.MINUTES);
        // 单独对key设置过期时间
        redisTemplate.boundValueOps("StringKey").expire(1, TimeUnit.MINUTES);
        redisTemplate.expire("StringKey", 1, TimeUnit.MINUTES);
    }

    /**
     * 从redis中删除数据，返回Boolean值，判断是否删除成功
     * delete()删除key的value
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:00
     */
    public void redisDelete() {
        Boolean result = redisTemplate.delete("StringKey1");
    }

    /**
     * 顺序递增/递减，实现计数器功能
     * increment(K key, Long dalta)顺序递增/递减，第二个参数为Long类型，正数则递增，负数则递减。返回递增/递减后的值
     * increment(K key, double delta)
     * value是数字类型，字符串类型没法增一
     * 有redisTemplate和stringRedisTemplate两种模板，redisTemplate模板使用时应为默认使用了JDK的序列化会在递增/低贱的过程中出现问题，stringRedisTemplate不会
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:18
     */
    @RequestMapping("/increment")
    public void redisCounter() {
        BoundValueOperations stringKey = redisTemplate.boundValueOps("StringKey");
        Object beforeValue = stringKey.get();
//        stringKey.increment(3L);// 异常（第一次可以，进行初始化，第二次增加的时候就会出错），java.io.EOFException: null
        Long stringKey1 = incr("StringKey", 3L);// 返回递增/递减后的值
        Object o = stringKey.get();
        System.out.println(beforeValue + "" + o);
    }

    /**
     * 顺序递增(increment)中出现的问题，和redisTemplate中的序列化有关。需要替换redisTemplate中的默认序列化器
     * 如果第一次的时候，redis中没有key会初始化数据为delta值，第二次的时候，在原来的delta值上再加上delta的值
     * <p>
     * RedisTemplate源码中，其默认的序列化器为JdkSerializationRedisSerializer，在序列化器进行序列化的时候，将key对应的value序列化为了字符串。使用的jdk对象序列化，序列化后的值有类信息、版本号等，所以是一个包含很多字母的字符串，所以根本无法加1。
     * GenericJackson2JsonRedisSerializer、Jackson2JsonRedisSerializer，在序列化器进行序列化的时候，是先将对象转为json，然后再保存到redis，所以，1在redis中是字符串1，所以无法进行加1。
     * GenericToStringSerializer、StringRedisSerializer，将字符串的值直接转为字节数组，所以保存到redis中是数字，所以可以进行加1
     *
     * @param key
     * @param delta
     * @return java.lang.Long
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:23
     */
    public Long incr(String key, long delta) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        redisTemplate.setKeySerializer(new StringRedisSerializer());// 设置key序列化
        redisTemplate.setValueSerializer(new StringRedisSerializer());// 设置value序列化
        return operations.increment(key, delta);
    }

    /**
     * setBit(K key, long offset, boolean value)key键对应的值value对应的ascii码,在offset的位置(从左向右数)变为value。
     * getBit(K key, long offset)判断指定的位置ASCII码的bit位是否为1。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:11
     */
    public void redisSetGetBit() {
        redisTemplate.opsForValue().setBit("stringValue", 1, false);
        String newStringValue = (String) redisTemplate.opsForValue().get("stringValue1");
        System.out.println("通过setBit(K key,long offset,boolean value)方法修改过后的值:" + newStringValue);

        boolean bitBoolean = redisTemplate.opsForValue().getBit("stringValue1", 1);
        System.out.println("通过getBit(K key,long offset)方法判断指定bit位的值是:" + bitBoolean);
    }

    /**
     * 批量操作
     * add(Set<TypedTuple<V>> var1) 向集合中插入多个元素,并设置分数
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 14:13
     */
    @RequestMapping("/muliti")
    public void redisMuliti() {
        DefaultTypedTuple<String> p1 = new DefaultTypedTuple<>("zSetVaule1", 2.1D);
        DefaultTypedTuple<String> p2 = new DefaultTypedTuple<>("zSetVaule2", 3.3D);
        redisTemplate.boundZSetOps("zSetKey").add(new HashSet<>(Arrays.asList(p1,p2)));
    }
}