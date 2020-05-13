package com.topsail.lettuce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 * @date 2020-05-11
 */
@Slf4j
@Configuration
public class CaffeineAutoConfiguration {

    @Bean("caffeineCacheManager")
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        Caffeine caffeine = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(50, TimeUnit.SECONDS);

        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

}
