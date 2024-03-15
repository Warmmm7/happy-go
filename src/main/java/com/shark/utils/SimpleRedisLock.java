package com.shark.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class SimpleRedisLock  implements ILock{
    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true)+"-";//做线程标识符...
    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程的标识...确定value
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁.. setnx。。。
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX+name, threadId, timeoutSec, TimeUnit.MINUTES);
        return BooleanUtil.isTrue(success);//使用工具类避免自动拆箱造成的空指针...
    }

    @Override
    public void unLock() {//避免线程释放锁不一致
        //获取线程的标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁的标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        if(threadId.equals(id)){
            //释放锁...
            stringRedisTemplate.delete(KEY_PREFIX+name);
        }

    }
}
