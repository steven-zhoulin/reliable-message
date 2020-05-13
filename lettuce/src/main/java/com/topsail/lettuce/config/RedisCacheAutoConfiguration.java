package com.topsail.lettuce.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Steven
 * @date 2020-05-11
 */
@Slf4j
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisCacheAutoConfiguration {


    private Set<RedisNode> parseToRedisNode(String[] hostAndPorts) {
        Set<RedisNode> nodes = new HashSet<>();
        for (String hostAndPort : hostAndPorts) {
            String[] ipPortPair = hostAndPort.split(":");
            String host = ipPortPair[0];
            int port = Integer.parseInt(ipPortPair[1]);
            nodes.add(new RedisNode(host, port));
        }
        return nodes;
    }

    private LettuceConnectionFactory createRedisConnectionFactory(String hostAndPort,
                                                                 String password,
                                                                 int database,
                                                                 int maxIdle,
                                                                 int minIdle,
                                                                 int maxActive,
                                                                 long maxWait,
                                                                 long timeOut,
                                                                 long shutdownTimeOut) {

        String[] hostAndPorts = StringUtils.split(hostAndPort, ',');
        RedisConfiguration redisConfiguration = null;
        if (hostAndPorts.length == 1) {

            String[] ipPortPair = hostAndPort.split(":");
            String host = ipPortPair[0];
            int port = Integer.parseInt(ipPortPair[1]);

            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(host);
            redisStandaloneConfiguration.setPort(port);
            redisStandaloneConfiguration.setDatabase(database);
            redisConfiguration = redisStandaloneConfiguration;
            log.info("单机模式");
        } else {

            RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
            redisClusterConfiguration.setClusterNodes(parseToRedisNode(hostAndPorts));
            redisClusterConfiguration.setPassword(password);
            redisConfiguration = redisClusterConfiguration;
            log.info("集群模式");
        }

        // 连接池配置
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);

        // Redis 客户端配置
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(timeOut))
            .shutdownTimeout(Duration.ofMillis(shutdownTimeOut))
            .poolConfig(genericObjectPoolConfig);

        LettuceClientConfiguration lettuceClientConfiguration = builder.build();

        // 根据配置和客户端配置创建连接
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration, lettuceClientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();

        return lettuceConnectionFactory;
    }

    @Primary
    @Bean("secLettuceConnectionFactory")
    public LettuceConnectionFactory secLettuceConnectionFactory(SecRedisConfig secRedisConfig) {
        log.info("secRedisConfig: {}", secRedisConfig);
        LettuceConnectionFactory redisConnectionFactory = createRedisConnectionFactory(secRedisConfig.nodes,
            secRedisConfig.password,
            secRedisConfig.database,
            secRedisConfig.maxIdle,
            secRedisConfig.minIdle,
            secRedisConfig.maxActive,
            secRedisConfig.maxWait,
            secRedisConfig.commandTimeout,
            secRedisConfig.shutdownTimeout);
        return redisConnectionFactory;
    }

    @Bean("upcLettuceConnectionFactory")
    public LettuceConnectionFactory upcLettuceConnectionFactory(UpcRedisConfig upcRedisConfig) {
        log.info("upcRedisConfig: {}", upcRedisConfig);
        LettuceConnectionFactory redisConnectionFactory = createRedisConnectionFactory(upcRedisConfig.nodes,
            upcRedisConfig.password,
            upcRedisConfig.database,
            upcRedisConfig.maxIdle,
            upcRedisConfig.minIdle,
            upcRedisConfig.maxActive,
            upcRedisConfig.maxWait,
            upcRedisConfig.commandTimeout,
            upcRedisConfig.shutdownTimeout);
        return redisConnectionFactory;
    }

    @Bean(name = "secRedisTemplate")
    public RedisTemplate secRedisTemplate(@Qualifier("secLettuceConnectionFactory") LettuceConnectionFactory secLettuceConnectionFactory) {
        RedisTemplate template = new RedisTemplate();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setConnectionFactory(secLettuceConnectionFactory);
        return template;
    }

    @Bean(name = "upcRedisTemplate")
    public RedisTemplate upcRedisTemplate(@Qualifier("upcLettuceConnectionFactory")LettuceConnectionFactory upcLettuceConnectionFactory) {
        RedisTemplate template = new RedisTemplate();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setConnectionFactory(upcLettuceConnectionFactory);
        return template;
    }

    @Primary
    @Bean("secCacheManager")
    public RedisCacheManager secCacheManager(@Qualifier("secLettuceConnectionFactory") LettuceConnectionFactory secLettuceConnectionFactory) {

        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer();

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(secLettuceConnectionFactory).cacheDefaults(cacheConfiguration).build();

        return redisCacheManager;
    }

    @Bean("upcCacheManager")
    public RedisCacheManager upcCacheManager(@Qualifier("upcLettuceConnectionFactory") LettuceConnectionFactory upcLettuceConnectionFactory) {

        JdkSerializationRedisSerializer redisSerializer = new JdkSerializationRedisSerializer();

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(upcLettuceConnectionFactory).cacheDefaults(cacheConfiguration).build();

        return redisCacheManager;
    }

}
