# springboot_redistemplate【spring-boot-starter-data-redis】
Redis支持五种数据类型：string（字符串），hash（哈希），list（列表），set（集合）及zset(sorted set：有序集合)。

spring-data-redis对jedis进行了封装  
redisTemplate  
1.连接池自动管理，提供了一个高度封装的 RedisTemplate 类  
2.将同一类型操作封装为operation接口：  
    ValueOperations：简单K-V操作(字符串)；  
    示例：  
        ValueOperations ops = redisTemplate.opsForValue();  
        ops.set("StringKey", "StringVaule");
    SetOperations：set类型数据操作；  
    ZSetOperations：zset类型数据操作；  
    HashOperations：针对map类型的数据操作；  
    ListOperations：针对list类型的数据操作；  
    StreamOperations：Stream是Redis 5.0引入的一种新数据类型，它以更抽象的方式模拟日志数据结构，但日志的本质仍然完好无损  
    GeoOperations：地理位置信息  
    HyperLogLogOperations：HyperLogLog是用来做基数统计的算法  
    ClusterOperations：cluster集群  
3.提供了对key的“bound”(绑定)便捷化操作API，可以通过bound封装指定的key，然后进行一系列的操作而无须“显式”的再次指定Key，即BoundKeyOperations：  
    BoundValueOperations  
    示例：  
        BoundValueOperations stringKey = redisTemplate.boundValueOps("StringKey");  
        stringKey.set("StringVaule");  
    BoundSetOperations  
    BoundListOperations  
    BoundSetOperations  
    BoundHashOperations  
4.将事务封装，由容器进行控制  
5.针对数据的“序列化/反序列化”，提供了多种可选择策略(RedisSerializer)  
    JdkSerializationRedisSerializer：POJO对象的存取场景，使用JDK本身序列化机制，将pojo类通过ObjectInputStream/ObjectOutputStream进行序列化操作，最终redis-server中将存储字节序列。是目前最常用的序列化策略。
    StringRedisSerializer：Key或者value为字符串的场景，根据指定的charset对数据的字节序列编码成string，是“new String(bytes, charset)”和“string.getBytes(charset)”的直接封装。是最轻量级和高效的策略。
    JacksonJsonRedisSerializer：jackson-json工具提供了javabean与json之间的转换能力，可以将pojo实例序列化成json格式存储在redis中，也可以将json格式的数据转换成pojo实例。因为jackson工具在序列化和反序列化时，需要明确指定Class类型，因此此策略封装起来稍微复杂。【需要jackson-mapper-asl工具支持】  

    存数字的时候：
    RedisTemplate源码中，其默认的序列化器为JdkSerializationRedisSerializer，在序列化器进行序列化的时候，将key对应的value序列化为了字符串。使用的jdk对象序列化，序列化后的值有类信息、版本号等，所以是一个包含很多字母的字符串，所以根本无法加1。
    GenericJackson2JsonRedisSerializer、Jackson2JsonRedisSerializer，在序列化器进行序列化的时候，是先将对象转为json，然后再保存到redis，所以，1在redis中是字符串1，所以无法进行加1。
    GenericToStringSerializer、StringRedisSerializer，将字符串的值直接转为字节数组，所以保存到redis中是数字，所以可以进行加1


1. 建议使用泛型此种方式，在使用中就不会涉及到类型的强制转换。例：RedisTemplate<String, String> redisTemplateString;
2. 指定泛型的时候推荐使用@Resource注解 指定泛型的时候使用@Resource注解。【当配置了redisConfig后，什么注解都能使用】  
    @Autowired默认按照类型装配的。也就是说，想要获取RedisTemplate< String, Object>的Bean，要根据名字装配。那么自然想到使用@Resource，它默认按照名字装配  
redis 自动注入时，泛型只能有两种  
        1：RedisTemplate<Object, Object>  
        2：StringRedisTemplate extends RedisTemplate<String, String>  
    如果项目中使用的泛型不是这两种，可以在导入的时候不指明泛型，否则自动导入会报错:  
        Description:  
        A component required a bean of type 'org.springframework.data.redis.core.RedisTemplate' that could not be found.  
        Action:  
        Consider defining a bean of type 'org.springframework.data.redis.core.RedisTemplate' in your configuration.  
    可以改为不使用泛型的redisTemplate或者是重新配置【如果针对自动配置类型添加自己的Bean，它将取代默认的】
RedisTemplate使用时常见问题：  
    redisTemplate 中存取数据都是字节数组。当redis中存入的数据是可读形式而非字节数组时，使用redisTemplate取值的时候会无法获取导出数据，获得的值为null。可以使用 StringRedisTemplate 试试。

StringRedisTemplate  
两者的数据是不共通的；也就是说StringRedisTemplate只能管理StringRedisTemplate里面的数据，RedisTemplate只能管理RedisTemplate中的数据。  
其实他们两者之间的区别主要在于他们使用的序列化类:  
    RedisTemplate使用的是JdkSerializationRedisSerializer  
    StringRedisTemplate使用的是StringRedisSerializer  
使用时注意事项：  
    当你的redis数据库里面本来存的是字符串数据或者你要存取的数据就是字符串类型数据的时候，那么你就使用StringRedisTemplate即可。  
    但是如果你的数据是复杂的对象类型，而取出的时候又不想做任何的数据转换，直接从Redis里面取出一个对象，那么使用RedisTemplate是更好的选择。

RedisTemplate和StringRedisTemplate的区别和联系：  
StringRedisTemplate和RedisTemplate，这两个类是springboot-data-redis对Redis进行操作的实现类。  
打开StringRedisTemplate和RedisTemplate的源码，发现StringRedisTemplate继承了RedisTemplate，  
    一般来说子类应该比父类有用更强大的功能，而此处却不是，因为RedisTemplate是泛型类，而在StringRedisTemplate继承RedisTemplate类时，则是指定了泛型的类型，两个String。  
这就直接导致了，StringRedisTemplate只能处理String-String的键值对数据，而RedisTemplate则可以处理任何类型的键值对。  
第一点，StringRedisTemplate继承了RedisTemplate。  
第二点，RedisTemplate是一个泛型类，而StringRedisTemplate则不是。
第三点，StringRedisTemplate只能对key=String，value=String的键值对进行操作，RedisTemplate可以对任何类型的key-value键值对操作。  
第四点，是他们各自序列化的方式不同，但最终都是得到了一个字节数组，殊途同归  
    StringRedisTemplate使用的是StringRedisSerializer类；RedisTemplate使用的是JdkSerializationRedisSerializer类。反序列化，则是一个得到String，一个得到Object

    遇到的报错【springboot版本2.3.3.RELEASE的时候报错，到版本2.4.5就不会报错了。报错解决办法pom中加commons-pool2包】
    Caused by: java.lang.ClassNotFoundException: org.apache.commons.pool2.impl.GenericObjectPoolConfig
        at java.net.URLClassLoader.findClass(URLClassLoader.java:381) ~[na:1.8.0_181]
        at java.lang.ClassLoader.loadClass(ClassLoader.java:424) ~[na:1.8.0_181]
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349) ~[na:1.8.0_181]
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357) ~[na:1.8.0_181]
	    ... 60 common frames omitted

jedis redisson lettuce redistemplate Stringredistemplate  
jedis：使用阻塞的I/O，且其方法调用都是同步的，程序流需要等到sockets处理完I/O才能执行，不支持异步。Jedis客户端实例不是线程安全的，所以需要通过连接池来使用Jedis。  
redisson：基于Netty框架的事件驱动的通信层，其方法调用是异步的。Redisson的API是线程安全的，所以可以操作单个Redisson连接来完成各种操作。  
lettuce：基于Netty框架的事件驱动的通信层，其方法调用是异步的。Lettuce的API是线程安全的，所以可以操作单个Lettuce连接来完成各种操作。能够支持redis4，需要java8及以上。是基于netty实现的与redis进行同步和异步的通信。
redistemplate：Spring框架提供了对Lettuce的封装RedisTemplate，它让Spring框架体系可以更方便的操作Redis。

    jedis使直接连接redis server,如果在多线程环境下是非线程安全的，这个时候只有使用连接池，为每个jedis实例增加物理连接；
    lettuce的连接是基于Netty的，连接实例（StatefulRedisConnection）可以在多个线程间并发访问，StatefulRedisConnection是线程安全的，所以一个连接实例可以满足多线程环境下的并发访问，当然这也是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例。
    Redisson实现了分布式和可扩展的Java数据结构，和Jedis相比，功能较为简单，不支持字符串操作，不支持排序、事务、管道、分区等Redis特性。Redisson的宗旨是促进使用者对Redis的关注分离，从而让使用者能够将精力更集中地放在处理业务逻辑上。
    优先使用Lettuce，如果需要分布式锁，分布式集合等分布式的高级特性，添加Redisson结合使用，因为Redisson本身对字符串的操作支持很差。


