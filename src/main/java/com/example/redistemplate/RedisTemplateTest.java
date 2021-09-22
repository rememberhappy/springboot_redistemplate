package com.example.redistemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.TimeUnit;

/**
 * @Author zhangdj
 * @Date 2021/5/19:14:35
 * @Description redistemplate类的简单使用，基本使用
 */
@Controller
public class RedisTemplateTest {
    @Autowired
    private RedisTemplate redisTemplate;

    //    删除key
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    //    删除多个key
    public void deleteKey(String... keys) {
        redisTemplate.delete(keys);
    }

    //    指定key的失效时间
    public void expire(String key, long time) {
        redisTemplate.expire(key, time, TimeUnit.MINUTES);
    }

    //    根据key获取过期时间
    public long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    //    判断key是否存在
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}