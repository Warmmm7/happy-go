package com.shark.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.entity.Shop;
import com.shark.mapper.ShopMapper;
import com.shark.service.IShopService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

}
