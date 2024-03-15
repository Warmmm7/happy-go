package com.shark.service.impl;

import com.shark.dto.Result;
import com.shark.entity.SeckillVoucher;
import com.shark.entity.Voucher;
import com.shark.entity.VoucherOrder;
import com.shark.mapper.VoucherOrderMapper;
import com.shark.service.ISeckillVoucherService;
import com.shark.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.utils.RedisIdWorker;
import com.shark.utils.SimpleRedisLock;
import com.shark.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    public Result seckillVoucher(Long voucherId) {
        //查询id 优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //判断秒杀是否开始 结束。。
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            //还没开始 返回错误提示
            return Result.fail("抢购尚未开始...");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            //抢购已经结束啦！
            return Result.fail("抢购已经结束...");
        }
        //判断库存是否足够...
        if (voucher.getStock() < 1) {
            return Result.fail("商品暂无...");
        }
        return createVoucherOrder(voucherId);

    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        //要求一人一单！！！！
        Long userId = UserHolder.getUser().getId();

        //        不适合多集群 jdk自带锁
//        Long userId = UserHolder.getUser().getId();
//        synchronized (userId.toString().intern()) {
//            //不代理直接调this 影响事务功能 拿到代理对象避免问题
//            IVoucherOrderService proxy = (IVoucherOrderService)AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);//用代理对象调用 springboot管理
//            //先获取锁！ 再创建事务 事务提交完成释放锁！
//        }


        //采用分布式锁！！！！！！
        //SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);

        RLock redisLock = redissonClient.getLock("lock:order:" + userId);//redisson实现...

        boolean isLock =redisLock.tryLock();

        //判断是否获取锁成功
        if(!isLock) {
            //获取锁失败了...
            return Result.fail("不允许重复下单...");
        }

        try {
            //1.先查询订单
            int count = query()
                    .eq("user_id", userId)
                    .eq("voucher_id", voucherId)
                    .count();
            //2.判断是否存在...
            if (count > 0) {//至少下过单 拒绝
                return Result.fail("一位用户最多购买一单，已有订单完成");
            }
            //扣减库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock -1")
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0) //简单悲观锁的实现 判断库存前后是否一样
                    .update();
            if (!success) {
                //扣减失败
                return Result.fail("商品暂无...");
            }


            // 创建并返回订单id
            VoucherOrder voucherOrder = new VoucherOrder();
            long orderId = redisIdWorker.nextId("order");//订单id
            voucherOrder.setId(orderId);

            voucherOrder.setUserId(userId);//用户id

            voucherOrder.setVoucherId(voucherId);//抢购商品id
            save(voucherOrder);

            return Result.ok(orderId);
        } finally {
            redisLock.unlock();
        }

    }
}
