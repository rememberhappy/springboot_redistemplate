package com.example.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;


/**
 * redisson分布式锁的应用
 * tryLock(),lock()
 *
 * @Author zhangdj
 * @Date 2021/6/10:15:51
 */
public class RedissonLock {
    private static Logger log = LoggerFactory.getLogger("RedissonLock");
    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("这是一个请求或者一个定时任务")
    public void useTheTryLock() {
        RLock lock = redissonClient.getLock("ScheduledTask.awardCertificateWithRuningTrain");
        try {
            // trylock是条件判断，当true时，上锁，否则false，没有获取锁
            if (lock.tryLock()) {
                log.info("useTheLock 获取锁成功");
                // TODO: 2021/6/10  业务处理
            } else {
                //未获取到锁
                log.info("useTheLock 获取锁失败");
            }
        } catch (Exception e) {
            log.error("useTheLock error");
        } finally {
            // isHeldByCurrentThread()查询当前线程是否保持此锁定
            if (lock.isHeldByCurrentThread()) {
                // 释放锁
                lock.unlock();
            }
            log.info("useTheLock finish");
        }
    }
    public void useTheTryLockTime() {
        RLock lock = redissonClient.getLock("ScheduledTask.awardCertificateWithRuningTrain");
        try {
            /**
             * tryLock(long waitTime, long leaseTime, TimeUnit unit)
             * 未获取到锁时，waitTime 等待多长时间，leaseTime锁的过期时间，unit时间单位
             */
            if (lock.tryLock(0L,-1L, TimeUnit.SECONDS)) {
                log.info("useTheLock 获取锁成功");
                // TODO: 2021/6/10  业务处理
            } else {
                //未获取到锁
                log.info("useTheLock 获取锁失败");
            }
        } catch (Exception e) {
            log.error("useTheLock error");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            log.info("useTheLock finish");
        }
    }
    public void useTheLock() {
        RLock lock = redissonClient.getLock("ScheduledTask.awardCertificateWithRuningTrain");
        // 如果锁不可用，那么当前的线程就会被禁用，在获得锁之前处于休眠状态。
        lock.lock();
        /**
         * lock(long leaseTime, TimeUnit unit)
         * 等待时间，不会一直处于等待中
         */
        lock.lock(0, TimeUnit.MINUTES);
        try {
            // TODO: 2021/6/10  业务处理
        } catch (Exception e) {
            log.error("useTheLock error");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            log.info("useTheLock finish");
        }
    }
}