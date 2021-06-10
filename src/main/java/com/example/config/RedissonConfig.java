package com.example.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author zhangdj
 * @Date 2021/6/10:15:34
 */
@Configuration
@ConditionalOnClass({RedisAutoConfiguration.class})
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private Integer port;
    @Value("${spring.redis.database}")
    private Integer database;
    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient() {
        String url = String.format("redis://%s:%d", this.host, this.port);
        Config config = new Config();
        if (StringUtils.isNotEmpty(this.password)) {
            config.useSingleServer().setAddress(url).setDatabase(this.database).setPassword(this.password);
        } else {
            config.useSingleServer().setAddress(url).setDatabase(this.database);
        }
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}