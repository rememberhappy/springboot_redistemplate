package com.example.redisson;

import com.example.domain.Student;
import jodd.util.MathUtil;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 功能注释
 *
 * @author zhangdj
 * @version 1.0.0
 * @createdAt 2022/8/3 15:06
 * @updatedAt 2022/8/3 15:06
 */
@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Autowired
    private RedissonClient redissonClient;

    static long i = 20;

    // 查询所有的 keys
    @GetMapping("/key/getAll")
    public String getAll() {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keys1 = keys.getKeys();
        keys1.forEach(System.out::println);
        return keys.toString();
    }

    @GetMapping("/stringSet/{key}")
    public String stringSet(@PathVariable String key) {
        // 设置字符串
        RBucket<String> keyObj = redissonClient.getBucket(key);
        keyObj.set("value");
        // 设置值的同时 设置过期时间
        keyObj.set("300", 360000000L, TimeUnit.SECONDS);
        return key;
    }

    @GetMapping("/stringGet/{key}")
    public String stringGet(@PathVariable String key) {
        // 设置字符串
        RBucket<String> keyObj = redissonClient.getBucket(key);
        return keyObj.get();
    }

    @GetMapping("/hashPut/{key}")
    public String hsetPut(@PathVariable String key) {

        Student student = new Student();
        student.setId(MathUtil.randomLong(1, 20));
        student.setName(key);
        // 存放 Hash
        RMap<String, Student> ss = redissonClient.getMap("UR");
        ss.put(student.getId().toString(), student);
        return student.toString();
    }

    @GetMapping("/hashGet/{key}")
    public String hashGet(@PathVariable String key) {
        // hash 查询
        RMap<String, Student> ss = redissonClient.getMap("UR");
        Student student = ss.get(key);
        return student.toString();
    }

    // ================== ==============读写锁测试 =============================

    @GetMapping("/rw/set/{key}")
    public void rwSet() {
        RBucket<String> lsCount = redissonClient.getBucket("LS_COUNT");
        lsCount.set("300", 360000000L, TimeUnit.SECONDS);
    }

    // 减法运算
    @GetMapping("/jf")
    public void jf() {
        String key = "S_COUNT";
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);

        if (!atomicLong.isExists()) {
            atomicLong.set(300L);
        }
        while (i == 0) {
            if (atomicLong.get() > 0) {
                long l = atomicLong.getAndDecrement();
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i--;
                System.out.println(Thread.currentThread().getName() + "->" + i + "->" + l);
            }
        }
    }

    @GetMapping("/rw/get")
    public String rwGet() {
        String key = "S_COUNT";
        Runnable r = () -> {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            if (!atomicLong.isExists()) {
                atomicLong.set(300L);
            }
            if (atomicLong.get() > 0) {
                long l = atomicLong.getAndDecrement();
                i--;
                System.out.println(Thread.currentThread().getName() + "->" + i + "->" + l);
            }
        };
        while (i != 0) {
            new Thread(r).start();
//            new Thread(r).run();
//            new Thread(r).run();
//            new Thread(r).run();
//            new Thread(r).run();
        }
        RBucket<String> bucket = redissonClient.getBucket(key);
        String s = bucket.get();
        System.out.println("================线程已结束================================" + s);
        return s;
    }

}