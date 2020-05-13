package com.topsail.lettuce.config;

import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Steven
 * @date 2020-05-11
 */
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "cache.redis.sec")
public class SecRedisConfig {

    /**
     * Redis 节点地址，格式: ip:port, 多个地址以,分割
     */
    public String nodes;

    /**
     * 密码
     */
    public String password = "";

    /**
     * Redis 默认情况下有 16 个库，这里配置具体使用的库，默认是 0
     */
    public int database = 0;

    /**
     * 连接池最大连接数（使用负值表示没有限制） 默认 8
     */
    public int maxActive = 8;

    /**
     * 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
     */
    public int maxWait = -1;

    /**
     * 连接池中的最大空闲连接 默认 8
     */
    public int maxIdle = 8;

    /**
     * 连接池中的最小空闲连接 默认 1
     */
    public int minIdle = 1;

    /**
     * 指令执行的超时时间 默认 2000 毫秒
     */
    public int commandTimeout = 2000;

    /**
     * 关闭时的超时时间 默认 1000 毫秒
     */
    public int shutdownTimeout = 1000;
}
