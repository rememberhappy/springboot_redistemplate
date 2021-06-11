package com.example.lock;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;


/**
 * redisson分布式锁
 * 1. 互斥
 *      在分布式高并发的条件下，我们最需要保证，同一时刻只能有一个线程获得锁，这是最基本的一点。
 * 2. 防止死锁
 *      在分布式高并发的条件下，比如有个线程获得锁的同时，还没有来得及去释放锁，就因为系统故障或者其它原因使它无法执行释放锁的命令,导致其它线程都无法获得锁，造成死锁。
 *      所以分布式非常有必要设置锁的 有效时间 ，确保系统出现故障后，在一定时间内能够主动去释放锁，避免造成死锁的情况。
 * 3. 性能
 *      对于访问量大的共享资源，需要考虑减少锁等待的时间，避免导致大量线程阻塞。
 *      锁的颗粒度要尽量小。比如你要通过锁来减库存，那这个锁的名称你可以设置成是商品的ID,而不是任取名称。这样这个锁只对当前商品有效,锁的颗粒度小。
 *      锁的范围尽量要小 。比如只要锁2行代码就可以解决问题的，那就不要去锁10行代码了。
 * 4. 重入
 *      同一个线程可以重复拿到同一个资源的锁。重入锁非常有利于资源的高效利用。
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
             * 尝试加锁，最多等待100秒，上锁以后10秒自动解锁。加锁成功后返回true
             * Redisson 提供了可以指定leaseTime参数的加锁方法来指定加锁的时间。超过这个时间后锁便自动解开了，不会延长锁的有效期。
             */
            if (lock.tryLock(100,10, TimeUnit.SECONDS)) {
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
         * 加锁以后10秒钟自动解锁，无需调用unlock方法手动解锁
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
    /**
     *
     * 1. Redisson提供的分布式锁是支持锁自动续期的，也就是说，如果线程仍旧没有执行完，
     * 那么redisson会自动给redis中的目标key延长超时时间，这在Redisson中称之为 Watch Dog 机制
     * 2. 如果负责储存这个分布式锁的Redis节点宕机以后，而且这个锁正好处于锁住的状态时，这个锁会出现锁死的状态。在Redisson实例被关闭前，
     * Watch Dog不断的延长锁的有效期。为了解决这个问题 程序释放锁操作一定要放到 finally {} 中
     *
     * 为了避免这两种情况的发生，Redisson内部提供了一个【监控锁的看门狗】，它的作用是在Redisson实例被关闭前，
     * 不断的延长锁的有效期。默认情况下，看门狗的检查锁的超时时间是30秒钟，也可以通过修改Config.lockWatchdogTimeout来另行指定。
     * watch dog 在当前节点存活时每 10s 给分布式锁的key续期到 30s；
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/6/10 20:20
     */
    private void useTheLockWatchDog() throws InterruptedException {
        //1. 普通的可重入锁
        RLock lock = redissonClient.getLock("generalLock");

        // 拿锁失败时会不停的重试
        // 具有Watch Dog 自动延期机制 默认续30s 每隔30/3=10 秒续到30s
        // 加锁得业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s后自动删除
        lock.lock();

        // 拿锁失败时会不停的重试
        // 没有Watch Dog ，10s后自动释放
        // 在锁时间到了以后，不会自动续期,没有Watch Dog
        lock.lock(10, TimeUnit.SECONDS);

        // 尝试拿锁10s后停止重试,返回false
        // 具有Watch Dog 自动延期机制 默认续30s
        boolean res1 = lock.tryLock(10, TimeUnit.SECONDS);

        // 尝试拿锁100s后停止重试,返回false
        // 没有Watch Dog ，10s后自动释放
        boolean res2 = lock.tryLock(100, 10, TimeUnit.SECONDS);

        //2. 公平锁 保证 Redisson 客户端线程将以其请求的顺序获得锁
        RLock fairLock = redissonClient.getFairLock("fairLock");

        //3. 读写锁 没错与JDK中ReentrantLock的读写锁效果一样
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readWriteLock");
        readWriteLock.readLock().lock();
        readWriteLock.writeLock().lock();
    }

    /**
     * 公平锁
     * 它保证了当多个Redisson客户端线程同时请求加锁时，优先分配给先发出请求的线程。
     * 所有请求线程会在一个队列中排队，当某个线程出现宕机时，Redisson会等待5秒后继续下一个线程，
     * 也就是说如果前面有5个线程都处于等待状态，那么后面的线程会等待至少25秒。
     *
     * @param
     * @return void
     * @Throws
     * @Author zhangdj
     * @date 2021/6/10 19:37
     */
    public void useTheFairLock() {
        RLock lock = redissonClient.getFairLock("anyLock");
        // 如果锁不可用，那么当前的线程就会被禁用，在获得锁之前处于休眠状态。
        lock.lock();
        /**
         * lock(long leaseTime, TimeUnit unit)
         * 加锁以后10秒钟自动解锁，无需调用unlock方法手动解锁
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