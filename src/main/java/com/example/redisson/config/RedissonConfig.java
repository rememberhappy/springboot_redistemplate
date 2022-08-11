package com.example.redisson.config;

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
//@Log4j2(topic = "RedissonConfig")
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
        Config config = new Config();
        String url = String.format("redis://%s:%d", this.host, this.port);
        if (StringUtils.isNotEmpty(this.password)) {
            config.useSingleServer().setAddress(url).setDatabase(this.database).setPassword(this.password);
        } else {
            config.useSingleServer().setAddress(url).setDatabase(this.database);
        }
        // 第二种方式：可以设置 Redisson 单独的配置文件，通过加载这个文件来对 Redisson 进行初始化配置。如果是JSON，则需要通过 Config.fromJSON();
        // config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-production.yml"));
        // 第三种方式：在 yml 中配置
        /*spring:
          redis:
            redisson:
              config: classpath:redisson-config.yaml*/
        // 第一种方式
        return Redisson.create(config);
    }
}