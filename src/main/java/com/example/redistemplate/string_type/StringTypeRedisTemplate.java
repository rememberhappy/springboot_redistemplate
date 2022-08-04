package com.example.redistemplate.string_type;

import com.example.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhangdj
 * @Date 2021/5/19:14:42
 * @Description redis中操作字符串类型【String】的数据
 */
@RestController
@RequestMapping("/stringtest")
public class StringTypeRedisTemplate {
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    /**
    1. 建议使用泛型此种方式，在使用中就不会涉及到类型的强制转换。
    2. 指定泛型的时候使用@Resource注解。【当配置了redisConfig后，什么注解都能使用】
    @Autowired 默认按照类型装配的。也就是说，想要获取RedisTemplate< String, Object>的Bean，要根据名字装配。那么自然想到使用@Resource，它默认按照名字装配
     */
    @Resource
    RedisTemplate<String, String> redisTemplateString;
    /**
    redis 自动导入时，泛型只能有两种
        1：RedisTemplate<Object, Object>
        2：StringRedisTemplate extends RedisTemplate<String, String>
    如果项目中使用的泛型不是这两种，可以在导入的时候不指明泛型，否则自动导入会报错:
        Description:
        A component required a bean of type 'org.springframework.data.redis.core.RedisTemplate' that could not be found.
        Action:
        Consider defining a bean of type 'org.springframework.data.redis.core.RedisTemplate' in your configuration.
    可以改为不使用泛型的redisTemplate或者是重新配置【如果针对自动配置类型添加自己的Bean，它将取代默认的】
     */
    @Resource
    RedisTemplate<String, Student> redisTemplateStudent;

    /**
     * 操作redis中的字符串类型的数据的三种方式
     * set(K key, V value)新增一个字符串类型的值,key是键，value是值。
     * set(K key, V value, long timeout, TimeUnit unit)在新增数据得同时设置变量值的过期时间。
     * set(K key, V value, long offset)覆盖从指定位置开始的值。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:40
     */
    @RequestMapping("/findvalue")
    public String redisTemplateTest() {
        //1、通过redisTemplate设置值，boundValueOps：操作字符串类型的数据
        redisTemplate.boundValueOps("StringKey1").set("StringValue");
        redisTemplateString.opsForValue().set("StringKey1_1", "StringValue1_1");
        Student student = new Student();
        student.setId(1l);
        student.setName("张三");
        student.setAddress("北京");
        student.setAge(18);
        student.setClassname("五年级");
        redisTemplateStudent.opsForValue().set("StringKey1_student", student);
        redisTemplate.boundValueOps("StringKey2").set("StringValue", 1, TimeUnit.MINUTES);

        //2、通过BoundValueOperations设置值， boundValueOps：操作字符串类型的数据
        BoundValueOperations<String, String> stringKey = redisTemplate.boundValueOps("StringKey3");
        stringKey.set("StringVaule1");// 此处的值会被后面的替换
        stringKey.set("StringValue2", 1, TimeUnit.MINUTES);

        //3、通过ValueOperations设置值， opsForValue：操作字符串类型的数据
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("StringKey4", "StringVaule");
        ops.set("StringValue5", "StringVaule", 1, TimeUnit.MINUTES);

        // 覆盖从指定位置开始的值
        redisTemplate.opsForValue().set("absentValue", "aaaaaaaaa");
        redisTemplate.opsForValue().set("absentValue", "dd", 2);// 将aaaaaaaaa值替换成了"a"dd"aaaa"从第二位开始，替换的长度是四个，被替换的字符串中2，3，4，5位就是替换的字符串
        String overrideString = redisTemplate.opsForValue().get("absentValue") + "";// 查到的值是：a，这个有点问题
        System.out.println("通过set(K key, V value, long offset)方法覆盖部分的值:" + overrideString);
        return "index";
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
        String str1 = redisTemplate.boundValueOps("StringKey1").get();
        String s1 = redisTemplateString.boundValueOps("StringKey1_1").get();
        Student student1 = redisTemplateStudent.boundValueOps("StringKey1_student").get();
        System.out.println(str1 + "," + s1 + "," + student1.toString());
        String stringKey1_student = redisTemplateString.boundValueOps("StringKey1_student").get();
        System.out.println(stringKey1_student);

        //2、通过BoundValueOperations获取值
        BoundValueOperations<String, String> stringKey = redisTemplate.boundValueOps("StringKey");
        String str2 = stringKey.get();
        BoundValueOperations<String, String> stringKey1 = redisTemplateString.boundValueOps("StringKey");
        String s2 = stringKey1.get();

        //3、通过ValueOperations获取值
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String str3 = ops.get("StringKey");
        ValueOperations<String, String> ops1 = redisTemplateString.opsForValue();
        String s3 = ops1.get("StringKey");
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
        String oldAndNewStringValue = redisTemplate.opsForValue().getAndSet("stringValue1", "ccc");
        System.out.print("通过getAndSet(K key, V value)方法获取原来的" + oldAndNewStringValue + ",");
        String newStringValue = redisTemplate.opsForValue().get("stringValue");
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
        Integer append1_1 = redisTemplateString.opsForValue().append("StringKey1_1", "append");
        Integer append1_student = redisTemplateStudent.opsForValue().append("StringKey1_student", "append");
        System.out.println(append1 + "," + append1_1 + "," + append1_student);
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
     * 有redisTemplate和stringRedisTemplate两种模板，redisTemplate模板使用时应为默认使用了JDK的序列化会在递增/递减的过程中出现问题，stringRedisTemplate不会
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:18
     */
    @RequestMapping("/increment")
    public void redisCounter() {
        BoundValueOperations<String, String> stringKey = redisTemplate.boundValueOps("StringKey");
        Object beforeValue = stringKey.get();
//        stringKey.increment(3L);// 异常（第一次可以，进行初始化，第二次增加的时候就会出错），java.io.EOFException: null
        // 返回递增/递减后的值
        Long stringKey1 = incr("StringKey", 3L);
        Object o = stringKey.get();
        System.out.println(beforeValue + "" + o);
    }

    /**
     * 顺序递增(increment)中出现的问题，和redisTemplate中的序列化有关。需要替换redisTemplate中的默认序列化器
     * 如果第一次的时候，redis中没有key会初始化数据为delta值，第二次的时候，在原来的delta值上再加上delta的值
     * <p>
     * RedisTemplate源码中，其默认的序列化器为JdkSerializationRedisSerializer，在序列化器进行序列化的时候，将key对应的value序列化为了字符串。使用的jdk对象序列化，序列化后的值有类信息、版本号等，所以是一个包含很多字母的字符串，所以根本无法加1。
     * GenericJackson2JsonRedisSerializer、Jackson2JsonRedisSerializer，
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
        String newStringValue = redisTemplate.opsForValue().get("stringValue1");
        System.out.println("通过setBit(K key,long offset,boolean value)方法修改过后的值:" + newStringValue);

        Boolean bitBoolean = redisTemplate.opsForValue().getBit("stringValue1", 1);
        System.out.println("通过getBit(K key,long offset)方法判断指定bit位的值是:" + bitBoolean);
    }

    /**
     * 批量操作
     * multiSet(Map<? extends K,? extends V> map) 设置map集合到redis。实际是新增了多个k-v数据到redis,是redis.set的一个批量操作
     * multiGet(Collection<K> keys) 根据集合取出对应的value值。是redis.get()的一个批量操作
     * multiSetIfAbsent(Map<? extends K,? extends V> map) 判断map是否存在，不存在则新增数据
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 14:13
     */
    @RequestMapping("/muliti")
    public void redisMuliti() {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("valueMap1", "map1");
        valueMap.put("valueMap2", "map2");
        valueMap.put("valueMap3", "map3");
        redisTemplate.opsForValue().multiSet(valueMap);

        //根据List集合取出对应的value值（list中的值是map的key）
        List<String> paraList = new ArrayList<>();
        paraList.add("valueMap1");
        paraList.add("valueMap2");
        paraList.add("valueMap3");
        paraList.add("valueMap4");
        List<String> valueList = redisTemplate.opsForValue().multiGet(paraList);
        for (String value : valueList) {
            System.out.println("通过multiGet(Collection<K> keys)方法获取map值:" + value);
        }

        // 判断map是否存在【只要有一个key在，就返回false，不对没有存在的数据做操作】，不存在则设置新值
        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("valueMap1", "map1");
        valueMap1.put("valueMap2", "map2");
        valueMap1.put("valueMap3", "map3");
        valueMap1.put("valueMap4", "map4");
        Boolean aBoolean = redisTemplate.opsForValue().multiSetIfAbsent(valueMap1);
        System.out.println("指定的key是否不存在：" + aBoolean);
    }
}