//package com.example.redisson.config;
//
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//
//import java.io.IOException;
//import java.util.Properties;
//
//@Configuration
////@Log4j2(topic = "RedissonConfig")
//public class Redisson2Config {
//
//    @Bean
//    public RedissonClient redisson() throws IOException {
//        //加载配置文件
//        Resource app = new ClassPathResource("application.yml");
//        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
//        //将加载的配置文件交给 YamlPropertiesFactoryBean
//        yamlPropertiesFactoryBean.setResources(app);
//        //将yml转换成 key：val
//        Properties properties = yamlPropertiesFactoryBean.getObject();
//        //本例子使用的是yaml格式的配置文件，读取使用Config.fromYAML，如果是Json文件，则使用Config.fromJSON
//        //默认读取dev环境
//        Config config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-dev.yml"));
//        if (properties != null) {
//            String active = properties.getProperty("spring.profiles.active");
//            //判断当前配置是什么环境
//            if ("test".equals(active)) {
//                config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-test.yml"));
//            } else if ("production".equals(active)) {
//                config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-production.yml"));
//            }
//        }
//        return Redisson.create(config);
//    }
//}
