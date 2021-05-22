package com.example.hash_type;

import com.example.domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhangdj
 * @Date 2021/5/20:14:29
 * @Description redis中操作哈希类型【hash】的数据
 * 在redis中，哈希类型是指Redis键值对中的值本身又是一个键值对结构，形如：value=[{field1，value1}，...{fieldN，valueN}]
 * Redis hash 是一个 string 类型的 field（字段） 和 value（值） 的映射表，hash 特别适合用于存储对象。
 * Redis 中每个 hash 可以存储 232 - 1 键值对（40多亿）。
 * 使用hash 省内存。在hash类型中，一个key可以对应多个多个field，一个field对应一个value。将一个对象存储为hash类型的好处之一：
 * 较于每个字段都单独存储成string类型来说，更能节约内存。
 */
@RestController
@RequestMapping("/hashtest")
public class HashTypeRedisTemplate {

    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    RedisTemplate<String, HashMap<String, String>> redisTemplateString;

    /**
     * 操作redis中的哈希类型的数据的三种方式
     * put(HK key, HV value)
     * HashOperations<K, HK, HV>三个泛型，第一个泛型是大key,第二个参数是小key，第三个参数第二个key的值，示例 k1=[{k2:v},{k3:v}]
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:40
     */
    @RequestMapping("/redisPut")
    public void redisPut() {
        //1、通过redisTemplate设置值
        redisTemplate.boundHashOps("HashKey1").put("SmallKey", "HashVaue");

        //2、通过BoundValueOperations设置值
        BoundHashOperations hashKey = redisTemplate.boundHashOps("HashKey2");
        hashKey.put("SmallKey1", "HashVaue");
        hashKey.put("SmallKey2", "HashVaue");

        //3、通过ValueOperations设置值 <K, HK, HV>
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put("HashKey3", "SmallKey1", "HashVaue");
        hashOps.put("HashKey3", "SmallKey2", "HashVaue");
    }

    /**
     * 从redis中获取哈希类型缓存的值
     * getKey() 获取BoundHashOperations对象中指定键
     * keys() 获取指定key的HK值，值[HK]是一个Set集合类型
     * values() 获取指定key的HV值，值[HV]是一个List集合类型
     * entries() 获取指定key下面的键值对，值是一个map[{HK,HV},{HK,HV}]类型
     * get(Object member) 获取K键中指定HK键的HV值
     * scan(ScanOptions options) 扫描特定键[H]所有值[{HK:HV},{HK:HV}]
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 15:52
     */
    @RequestMapping("/get")
    public void redisGet() {
        BoundHashOperations hashKey2 = redisTemplate.boundHashOps("HashKey2");

        // 获取大key，K
        Object key = hashKey2.getKey();
        System.out.println("获取设置的绑定key值:" + key);

        Set keys = redisTemplate.opsForHash().keys("HashKey2");
        Set keys1 = hashKey2.keys();
        System.out.print("获取指定key的HK值:");
        for (Object s : keys1) {
            System.out.print(s + " ");
        }
        System.out.println();

        // 获取key对用的hash值，所有的value【HV】
        List values = redisTemplate.opsForHash().values("HashKey2");
        List values1 = hashKey2.values();
        System.out.print("获取指定key的HV值:");
        for (Object s : values) {
            System.out.print(s + " ");
        }
        System.out.println();

        // 获取大key下所有的数据，以键值对的方式返回
        Map entries1 = redisTemplate.opsForHash().entries("HashKey2");
        Map<String, Object> entries = hashKey2.entries();
        System.out.print("获取指定key的{HK:HV}键值对的值:");
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            System.out.print(mapKey + ":" + mapValue + ",");
        }
        System.out.println();

        // 获取大key下的指定小key的值
        Object o = redisTemplate.opsForHash().get("HashKey2", "SmallKey1");
        System.out.println("获取键为：HashKey2的类型为Hash的 小key为SmallKey1的值：" + o);

        //遍历绑定键[H]获取所有值[{HK:HV},{HK:HV}]
        Cursor<Map.Entry<String, Object>> cursor = hashKey2.scan(ScanOptions.NONE);
        while (cursor.hasNext()) {
            Map.Entry<String, Object> entry = cursor.next();
            System.out.println("遍历绑定键获取所有值:" + entry.getKey() + "---" + entry.getValue());
        }
    }

    /**
     * putIfAbsent(HK key, HV value) 如果键不存在则新增,存在则不改变已经有的值。
     * 返回true，说明键不存在，进行新增数据
     * 返回false，说明键存在，不改变原有的值
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 13:49
     */
    @RequestMapping("/redisputIfAbsent")
    public void redisputIfAbsent() {
        Boolean m1 = redisTemplate.opsForHash().putIfAbsent("HashKey2", "m1", "n1");
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps("HashKey2");
        Boolean m2 = boundHashOperations.putIfAbsent("m2", "n2");
        Boolean m3 = boundHashOperations.putIfAbsent("m3", "n3");
        Boolean m4 = boundHashOperations.putIfAbsent("SmallKey1", "HashVaue");// 这个值已经存在
        System.out.println(m2 + "," + m3 + "," + m4);
        // 获取指定K下的键值对
        boundHashOperations.entries().forEach((m, n) -> System.out.println("新增不存在的键值对:" + m + "-" + n));
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
        redisTemplate.boundHashOps("StringKey").expire(1, TimeUnit.MINUTES);
        redisTemplate.expire("StringKey", 1, TimeUnit.MINUTES);
    }

    /**
     * size(H key)获取指定字符串的长度。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 12:14
     */
    public void redisSize() {
        Long hashKey1 = redisTemplate.opsForHash().size("HashKey2");
        Long hashKey2 = redisTemplate.boundHashOps("HashKey2").size();
        System.out.println("通过size(K key)方法获取字符串的长度:" + hashKey1);
    }

    /**
     * 顺序递增/递减，实现计数器功能
     * increment(K key, Long dalta)顺序递增/递减，第二个参数为Long类型，正数则递增，负数则递减。返回递增/递减后的值
     * increment(K key, double delta)
     * value是数字类型，字符串类型没法增一
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/19 16:18
     */
    @RequestMapping("/increment")
    public void redisCounter() {
        Long increment = redisTemplate.opsForHash().increment("HashKey2", "m2", 3l);
        BoundHashOperations hashType = redisTemplate.boundHashOps("HashKey2");
        Object beforeValue = hashType.get("m2");
        hashType.increment("m2", 3L);
        Object o = hashType.get("m2");
        System.out.println(beforeValue + "" + o);
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
        Long delete = redisTemplate.opsForHash().delete("HashKey2", "m3", "m2");
        long delSize = redisTemplate.boundHashOps("HashKey2").delete("m3", "m2");
        System.out.println("删除的键的个数:" + delete + ":" + delSize);
        redisTemplate.boundHashOps("HashKey2").entries().forEach((m, n) -> System.out.println("删除后剩余map键值对:" + m + "-" + n));
        Boolean result = redisTemplate.delete("StringKey1");
    }

    /**
     * 批量操作
     * putAll(Map<? extends HK,? extends HV> m) 根据map键[HK]批量获取map值[HV]
     * multiGet(Collection keys) 根据map键[HK]批量获取map值[HV]
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/5/20 14:13
     */
    @RequestMapping("/muliti")
    public void redisMuliti() {
        // 批量查询
        List list = new ArrayList<>(Arrays.asList("ww", "w1"));
        redisTemplate.boundHashOps("HashKey2").multiGet(list).forEach(v -> System.out.println("根据map键批量获取map值:" + v));

        // 批量插入
        HashMap<String, String> map = new HashMap<>();
        redisTemplate.boundHashOps("HashKey2").putAll(map);
    }
}