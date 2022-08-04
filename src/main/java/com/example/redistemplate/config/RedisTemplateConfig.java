package com.example.redistemplate.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * @Author zhangdj
 * @Date 2021/5/20:11:03
 * @Description
 */
@Configuration
@ConditionalOnClass({RedisAutoConfiguration.class})
public class RedisTemplateConfig extends CachingConfigurerSupport {

//    /**
//     * 注入 RedisConnectionFactory
//     */
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;

//    /**
//     * 实例化 RedisTemplate 对象
//     */
//    @Bean(name = "redisTemplate")
//    public RedisTemplate<String, ?> getRedisTemplate() {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        return redisTemplate;
//    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, ?> getRedisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // key采用String的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // value采用jackson的序列化方式。使用它操作普通字符串，会出现Could not read JSON。如果使用 StringRedisTemplate 的话，就没有这个顾虑了。否则使用：template.setValueSerializer(RedisSerializer.string());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 对hash类型的数据操作
     */
    @Bean
    public HashOperations<String, String, ?> hashOperations(RedisTemplate<String, ?> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 对链表类型的数据操作
     */
    @Bean
    public ListOperations<String, ?> listOperations(RedisTemplate<String, ?> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 对无序集合类型的数据操作
     */
    @Bean
    public SetOperations<String, ?> setOperations(RedisTemplate<String, ?> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 对有序集合类型的数据操作
     */
    @Bean
    public ZSetOperations<String, ?> zSetOperations(RedisTemplate<String, ?> redisTemplate) {
        return redisTemplate.opsForZSet();
    }

    /**
     * 实例化 ValueOperations 对象,可以使用 String 操作
     */
    @Bean
    public ValueOperations<String, ?> valueOperations(RedisTemplate<String, ?> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 字符串类型的 顺序递增(increment)/递减(decrement)中出现的问题【异常（第一次可以，进行初始化，第二次增加的时候就会出错），java.io.EOFException: null】。两种解决方案
     * 方案一：
     * 和 redisTemplate 中的序列化有关。需要替换 redisTemplate 中的默认序列化器
     * 如果第一次的时候，redis 中没有 key 会初始化数据为 delta 值，第二次的时候，在原来的 delta 值上再加上 delta 的值
     * 原理讲解：
     * RedisTemplate源码中，其默认的序列化器为JdkSerializationRedisSerializer，在序列化器进行序列化的时候，将key对应的value序列化为了字符串。使用的jdk对象序列化，序列化后的值有类信息、版本号等，所以是一个包含很多字母的字符串，所以根本无法加1。
     * GenericJackson2JsonRedisSerializer、Jackson2JsonRedisSerializer，在序列化器进行序列化的时候，是先将对象转为json，然后再保存到redis，所以，1在redis中是字符串1，所以无法进行加1。
     * GenericToStringSerializer、StringRedisSerializer，将字符串的值直接转为字节数组，所以保存到redis中是数字，所以可以进行加1
     * 方案二：
     * 使用 StringRedisTemplate
     */
    @Bean
    public ValueOperations<String, String> incrOperations(LettuceConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 设置key序列化
        template.setKeySerializer(RedisSerializer.string());
        // 设置value序列化
        template.setValueSerializer(RedisSerializer.string());
        template.afterPropertiesSet();
        return template.opsForValue();
    }

    /**
     * 自定义缓存key的生成策略。默认的生成策略是看不懂的(乱码内容)
     * 通过 Spring 的依赖注入特性进行自定义的配置注入并且此类是一个配置类可以更多程度的自定义配置
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    /**
     * 基于注解的 缓存设置会用到此设置
     * 配置自定义 缓存管理器
     * 当我们引入redis后，缓存管理器就由默认的 ConcurrentMapCacheManager 变成 RedisCacheManager 来进行管理了。
     * 我们在使用 RedisCacheManager 来操作 redis 时，底层操作默认使用的是 RedisTemplate，而 redisTemplate 是 redisAutoConfiguration 在项目启动时帮我们自动注册的组件，它默认使用的是 JDK 序列化机制。
     * 缓存管理器针对 @Cacheable 这种缓存注解起作用时，会自动的对数据进行存储，而不是手动通过 Redistemplate 进行存储，这样使用的还是jdk序列化机制，所以需要进行重写
     */
    @Bean
    public CacheManager cacheManager(LettuceConnectionFactory factory) {
//        // 以锁写入的方式创建RedisCacheWriter对象
//        RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(factory);
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(factory);
        // 设置json序列化机制，取代默认的jdk
        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        // 指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        RedisSerializationContext.SerializationPair<?> pair = RedisSerializationContext.SerializationPair
                .fromSerializer(jackson2JsonRedisSerializer);
        // 设置缓存配置格式
        RedisCacheConfiguration cacheCfg = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(pair)
                .entryTtl(Duration.ofSeconds(600))
                .disableCachingNullValues();
        return new RedisCacheManager(redisCacheWriter, cacheCfg);
    }
}