package com.example.annotationCache;

import com.alibaba.fastjson.JSONObject;
import com.example.dao.StudentDao;
import com.example.domain.Student;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * springBoot 使用Redis实现缓存注解的使用
 *
 * @author zhangdj
 * @version 1.0.0
 * @createdAt 2022/8/3 16:05
 * @updatedAt 2022/8/3 16:05
 */
@Transactional
@Service
public class AnnotationCacheService {

    @Resource
    private StudentDao studentDao;

    @Cacheable(value = "cache:customer", unless = "null == #result", key = "#id")
    public Student cacheOne(Integer id) {
        final Optional<Student> byId = studentDao.findById(id);
        return byId.orElse(null);
    }

    @Cacheable(value = "cache:customer", unless = "null == #result", key = "#id")
    public Student cacheOne2(Integer id) {
        final Optional<Student> byId = studentDao.findById(id);
        return byId.orElse(null);
    }

    @Cacheable(value = "cache:customer", unless = "null == #result", key = "#root.methodName + '.' + #id")
    public Student cacheOne3(Integer id) {
        final Optional<Student> byId = studentDao.findById(id);
        return byId.orElse(null);
    }

    // 这里缓存到redis，还有响应页面是String（加了很多转义符\,），不是Json格式
    @Cacheable(value = "cache:customer", unless = "null == #result", key = "#root.methodName + '.' + #id")
    public String cacheOne4(Integer id) {
        final Optional<Student> byId = studentDao.findById(id);
        // 对结果做处理
        return byId.map(JSONObject::toJSONString).orElse(null);
    }

    // todo 缓存json，不乱码已处理好,调整序列化和反序列化
    @Cacheable(value = "cache:customer", unless = "null == #result", key = "#root.methodName + '.' + #id")
    public Student cacheOne5(Integer id) {
        Optional<Student> byId = studentDao.findById(id);
        return byId.orElse(null);
    }

    @CacheEvict(value = "cache:customer", key = "'cacheOne5' + '.' + #id")
    public Object del(Integer id) {
        // 删除缓存后的逻辑
        return null;
    }

    @CacheEvict(value = "cache:customer", allEntries = true)
    public void del() {

    }

    @CacheEvict(value = "cache:all", allEntries = true)
    public void delall() {

    }

    @Cacheable(value = "cache:all")
    public List<Student> cacheList() {
        return studentDao.findAll();
    }

    // 先查询缓存，再校验是否一致，然后更新操作，比较实用，要清楚缓存的数据格式（明确业务和缓存模型数据）
    @CachePut(value = "cache:all", unless = "null == #result", key = "#root.methodName")
    public List<Student> cacheList2() {
        return studentDao.findAll();
    }
}