package com.shark.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.dto.Result;
import com.shark.entity.ShopType;
import com.shark.mapper.ShopTypeMapper;
import com.shark.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.shark.utils.RedisConstants.CACHE_TYPE_LIST;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryList() {
        String key  = CACHE_TYPE_LIST;
        String typeJson = stringRedisTemplate.opsForValue().get(key);
        //在缓存里查到了 直接返回缓存数据
        if(StrUtil.isNotBlank(typeJson)){
            List<ShopType> shopTypeList = JSONUtil.toList(typeJson, ShopType.class);
            return Result.ok(shopTypeList);
        }
        //没查到 去数据库查 并写道缓存里
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shopTypeList),60, TimeUnit.MINUTES);
        return Result.ok(shopTypeList);
    }
}
