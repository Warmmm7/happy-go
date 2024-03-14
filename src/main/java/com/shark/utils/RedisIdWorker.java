package com.shark.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1704067200L;
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;
    public RedisIdWorker(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成分布式ID... 拼凑的方法进行实现
     * @param keyPrefix
     * @return
     */
    public long nextId(String keyPrefix){
        //1.定义时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //2 生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));//获取当天日期
        Long count = stringRedisTemplate.opsForValue().increment("icr:" +keyPrefix+":"+date);//自增长
        //3 拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        System.out.println("second = "+ second);
    }
}
