package com.topsail.lettuce.controlloer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven
 * @date 2020-05-11
 */
@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    @Qualifier("secRedisTemplate")
    private RedisTemplate secRedisTemplate;

    @Autowired
    @Qualifier("upcRedisTemplate")
    private RedisTemplate upcRedisTemplate;

    @GetMapping(value = "/set")
    public void set() {
        secRedisTemplate.opsForValue().set("sec-k1", "sec-v1");
        upcRedisTemplate.opsForValue().set("upc-k1", "upc-v1");

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key-" + i, "value-" + i);
        }
        secRedisTemplate.opsForHash().putAll("SUPERUSR", map);

        log.info("set k1 = v1");
    }

    @GetMapping(value = "/get")
    public void get() {
        log.info("get k1 = {}", secRedisTemplate.opsForValue().get("sec-k1"));
        log.info("get k1 = {}", upcRedisTemplate.opsForValue().get("upc-k1"));

        Map<Object, Object> superusr = secRedisTemplate.opsForHash().entries("SUPERUSR");
        log.info("SUPERUSR: {}", superusr);
    }

    @SneakyThrows
    @GetMapping(value = "cache/sec/{id}")
    @Cacheable(value = "mycache", cacheManager = "secCacheManager")
    public String secCache(@PathVariable("id") String id) {
        TimeUnit.SECONDS.sleep(2);
        log.info("sec缓存未命中!");
        return RandomStringUtils.randomAlphabetic(8);
    }

    @SneakyThrows
    @GetMapping(value = "cache/upc/{id}")
    @Cacheable(value = "mycache", cacheManager = "upcCacheManager")
    public String upcCache(@PathVariable("id") String id) {
        TimeUnit.SECONDS.sleep(2);
        log.info("upc缓存未命中!");
        return RandomStringUtils.randomAlphabetic(8);
    }

    @SneakyThrows
    @GetMapping(value = "cache/local/{id}")
    @Cacheable(value = "mycache", cacheManager = "caffeineCacheManager")
    public String localCache(@PathVariable("id") String id) {
        TimeUnit.SECONDS.sleep(2);
        log.info("local缓存未命中!");
        return RandomStringUtils.randomAlphabetic(8);
    }

}
