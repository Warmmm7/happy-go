package com.shark.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.Result;
import com.shark.entity.ShopType;


public interface IShopTypeService extends IService<ShopType> {

    /**
     * 将商铺主页也缓存
     * @return
     */
    Result queryList();
}
