package com.shark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.Result;
import com.shark.entity.Shop;


public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);
}
