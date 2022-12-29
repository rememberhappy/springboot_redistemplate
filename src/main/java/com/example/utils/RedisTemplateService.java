package com.example.utils;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class RedisTemplateService<T> {

    @Resource
    private RedisTemplate<String, T> redisTemplate;



    public List<Long> getIdList(String key, Supplier<List<Long>> supplier) {
        if (!hasKey(key)) {
            List<Long> list = supplier.get();
            redisTemplate.opsForValue().set(key, JSONArray.toJSONString(list));
            return list;
        }
        Object o = redisTemplate.opsForValue().get(key);
        return o == Lists.newArrayList() ? null : JSONArray.parseArray((String) o, Long.class);
    }

    public <T> List<T> getDtoByIds(String prefix, List<Long> ids, Function<Long, T> fun) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RuntimeException("ids must not be empty");
        }
        List<String> keys = ids.stream().map(l -> String.format(prefix, l)).collect(Collectors.toList());
        List<Object> objects = redisTemplate.opsForValue().multiGet(keys);
        List<T> list = Lists.newArrayListWithCapacity(ids.size());
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            if (null == o) {
                T t = fun.apply(ids.get(i));
                redisTemplate.opsForValue().set(keys.get(i), t);
                list.add(t);
            } else {
                list.add((T) o);
            }
        }
        return list;
    }

    public <T> List<T> getDtoByConditionKey(String key,
                                            String prefix,
                                            Supplier<List<Long>> conditionQuerySupplier,
                                            Function<Long, T> dataFun) {
        List<Long> idList = getIdList(key, conditionQuerySupplier);
        if (CollectionUtils.isEmpty(idList)) {
            return Lists.newArrayList();
        }
        List<T> dtoByIds = getDtoByIds(prefix, idList, dataFun);
        return dtoByIds;
    }

    /**
     * 批量删掉实体下的集合key和涉及的数据key
     *
     * @param prefix
     * @param dataKey
     * @param ids
     */
    public void batchDel(String prefix, String dataKey, Long id, List<Long> ids) {
        try {
            StringRedisSerializer serializer = new StringRedisSerializer();
            Set<String> keys = redisTemplate.keys(prefix + "*");
            redisTemplate.executePipelined(((RedisCallback<?>) conn -> {
                if (CollectionUtils.isNotEmpty(keys)) {
                    keys.forEach(k -> conn.del(serializer.serialize(k)));
                }
                if (id != null) {
                    conn.del(serializer.serialize(String.format(dataKey, id)));
                }
                if (CollectionUtils.isNotEmpty(ids)) {
                    ids.forEach(i -> conn.del(serializer.serialize(String.format(dataKey, i))));
                }
                return null;
            }));
        } catch (Exception e) {
            ULogger.error("batchDel error: prefixKey={},dataKey={},ids={},exception={}", prefix, dataKey, JSONArray.toJSONString(ids), e);
        }
    }

    public void batchDel(String prefix, String dataKey, List<Long> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            batchDel(prefix, dataKey, null, ids);
        }
    }

    public void batchDel(String prefix, String dataKey, Long id) {
        if (id != null) {
            batchDel(prefix, dataKey, id, null);
        }
    }

    public void del(String dataKey, Long id) {
        try {
            redisTemplate.delete(String.format(dataKey, id));
        } catch (Exception e) {
            ULogger.error("del error: dataKey={},id={},exception={}", dataKey, id, e);
        }
    }

    public void del(String dataKey, Collection<Long> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            try {
                Set<String> set = ids.parallelStream().map(i -> String.format(dataKey, i)).collect(Collectors.toSet());
                redisTemplate.delete(set);
                //ids.forEach(i -> del(dataKey,i));
            } catch (Exception e) {
                ULogger.error("del error: dataKey={},ids={},exception={}", dataKey, JSONArray.toJSONString(ids), e);
            }
        }
    }

    public void batchDel(String prefix) {
        try {
            StringRedisSerializer serializer = new StringRedisSerializer();
            Set<String> keys = redisTemplate.keys(prefix + "*");
            redisTemplate.executePipelined(((RedisCallback<?>) conn -> {
                if (CollectionUtils.isNotEmpty(keys)) {
                    keys.forEach(k -> conn.del(serializer.serialize(k)));
                }
                return null;
            }));
        } catch (Exception e) {
            ULogger.error("batchDel error: prefixKey={},exception={}", prefix, e);
        }
    }
}
