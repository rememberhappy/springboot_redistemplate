package com.example.redisson.zset_type;

import org.redisson.api.RFuture;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

@RequestMapping("redisson")
public class zset_type {

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("scenario")
    public void scenarioTest() throws ExecutionException, InterruptedException {
        // 现在灰灰公司要做绩效考核 ，默认都是50分
        RScoredSortedSet<String> huihuiCompany = redissonClient.getScoredSortedSet("huihuiCompany");
        huihuiCompany.add(50, "lily");
        huihuiCompany.add(50, "lucy");
        huihuiCompany.add(50, "zhangsan");
        huihuiCompany.add(50, "lisi");
        huihuiCompany.add(50, "wangwu");
        huihuiCompany.add(50, "liuliu");

        //lisi给灰灰老师买了杯咖啡，加50分
        huihuiCompany.addScore("lisi", 50);
        //lily长得不错，60分
        huihuiCompany.addScore("lily", 50);
        //wangwu 不听话，减10分
        huihuiCompany.addScore("wangwu", -10);
        //liuliu说灰灰老师坏话，扣20
        huihuiCompany.addScore("liuliu", -20);
        //张三拍灰灰老师马屁，加10分
        huihuiCompany.addScore("zhangsan", 10);
        //lucy不上班，开除
        huihuiCompany.remove("lucy");
        RFuture<Collection<ScoredEntry<String>>> collectionRFuture = huihuiCompany.entryRangeReversedAsync(0, -1);
        Iterator<ScoredEntry<String>> iterator = collectionRFuture.get().iterator();
        System.out.println("绩效从高到低：");
        while (iterator.hasNext()) {
            ScoredEntry<String> next = iterator.next();
            System.out.println(next.getValue());
        }

        RFuture<Collection<ScoredEntry<String>>> collectionRFuture1 = huihuiCompany.entryRangeReversedAsync(0, 2);
        Iterator<ScoredEntry<String>> iterator1 = collectionRFuture1.get().iterator();
        System.out.println("绩效前三名：");
        while (iterator1.hasNext()) {
            ScoredEntry<String> next = iterator1.next();
            System.out.println(next.getValue());
        }
    }
}
