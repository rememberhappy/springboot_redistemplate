package com.example.utils;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redis工具类
 *
 * @author zhangdj
 * @version 1.0.0
 * @createdAt 2022/8/17 9:57
 * @updatedAt 2022/8/17 9:57
 */
@Component
public class RedisUtil<T> {

    @Resource
    private RedisTemplate<String, T> redisTemplate;
    @Autowired
    private HashOperations<String, String, T> hashOperations;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // =============================common============================

    /**
     * 功能描述：获取所有键值
     *
     * @param
     * @return
     * @author Jesson
     * @Date 2018/9/7 16:05
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 功能描述：指定缓存失效时间
     *
     * @param key
     * @return
     * @author Jesson
     * @date 2018/8/27 10:04
     */
    public Boolean expire(String key, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            if (time > 0) {
                return redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 功能描述：根据key 获取过期时间
     *
     * @param key 键，不能为null
     * @return 时间（s) 返回0 代表永久有效
     * @author Jesson
     * @date 2018/8/27 10:10
     */
    public Long getExpire(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 功能描述：判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     * @author Jesson
     * @date 2018/8/27 10:12
     */
    public boolean hasKey(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        Boolean b = redisTemplate.hasKey(key);
        return b != null && b;
    }

    /**
     * 功能描述：删除缓存
     *
     * @param key 可为多个
     * @return
     */
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public T getValueByKey(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, T value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public Long incr(String key, long delta) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public Long decr(String key, long delta) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public T hashGet(String key, String item) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return hashOperations.get(key, item);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<String, T> hashGet(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return hashOperations.entries(key);
    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hashSetMap(String key, Map<String, T> map) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            hashOperations.putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public Boolean hashSetTime(String key, Map<String, T> map, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            hashOperations.putAll(key, map);
            if (time > 0) {
                return expire(key, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hashSet(String key, String item, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            hashOperations.put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hashSet(String key, String item, T value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            hashOperations.put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hashDelete(String key, Object... item) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        hashOperations.delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hashHasKey(String key, String item) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return hashOperations.hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hashIncreasing(String key, String item, double by) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        return hashOperations.increment(key, item, by);
    }

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    public double hashDecrement(String key, String item, double by) {
        return hashOperations.increment(key, item, -by);
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<T> setGet(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public Boolean setGetHasKey(String key, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long setSet(String key, T... values) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public Long setSetAndTime(String key, long time, T... values) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public Long setGetSize(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public Long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<T> listGet(String key, long start, long end) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public Long listGetListSize(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public T listGetIndex(String key, long index) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean rListPush(String key, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将非对账类交易入库。
     *
     * @return 缓存键值对应的数据
     */
    public boolean lListPush(String key, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean listSet(String key, T value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean listSetList(String key, List<T> value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean listSetTime(String key, List<T> value, long time) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean listUpdateIndex(String key, long index, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public Long listRemove(String key, long count, T value) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key 不能为空");
        }
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    // ===============================组合使用=================================

    /**
     * @param prefix
     * @param id
     * @param fun
     * @return T
     * @Throws
     * @Author zhangdj
     * @date 2022/12/29 18:13
     */
    public T getAndSetById(String prefix, Long id, Function<Long, T> fun) {
        String key = String.format(prefix, id);
        T result = redisTemplate.opsForValue().get(key);
        if (null == result) {
            T t = fun.apply(id);
            redisTemplate.opsForValue().set(key, t);
            return t;
        }
        return result;
    }

    // 获取集合
    public List<T> getIdList(String key, Supplier<List<T>> supplier) {
        if (!hasKey(key)) {
            List<T> list = supplier.get();
            redisTemplate.opsForSet().add(key, list.toArray());
            return list;
        }
        Object o = stringRedisTemplate.opsForValue().get(key);
        return o == Lists.newArrayList() ? null : JSONArray.parseArray((String) o, Long.class);
    }

    /**
     * 功能描述：查询结果集
     *
     * @param key     键值
     * @param txnCode 交易码
     * @param decode  是否编码（true-Base64,false-解码Base64）
     * @return
     * @author Jesson
     * @Date 2018/8/31 16:34
     */
    public Map<String, T> hashGet(String key, Boolean decode, String txnCode) throws IOException {
        Map<String, T> keyMap = hashGet(key);
        if (!keyMap.isEmpty()) {
            return likeMap(keyMap, decode, txnCode);
        } else {
            return null;
        }
    }

    /**
     * @param map    源，要取数据的对象
     * @param decode 是否编码（true-Base64,false-解码Base64）
     * @param keys   匹配的KEY
     * @return java.util.Map<java.lang.String, T>
     * @Throws
     * @Author zhangdj
     * @date 2022/12/29 18:06
     */
    public Map<String, T> likeMap(Map<String, T> map, boolean decode, String... keys) throws IOException {
        Map<String, T> resultMap = new HashMap<>();
        if (keys.length > 0 && !map.isEmpty()) {
            for (String key : keys) {
                for (Map.Entry<String, T> entry : map.entrySet()) {
                    // 忽略大小写判断
                    Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(entry.getKey());
                    if (matcher.find()) {
                        if (decode) {
                            resultMap.put(entry.getKey(), entry.getValue());
                        } else {
                            resultMap.put(entry.getKey(), (T) decoder(entry.getValue().toString()));
                        }
                    }
                }
            }
        }
        return resultMap;
    }

    private byte[] decoder(String endcoderStr) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(endcoderStr);
    }
}