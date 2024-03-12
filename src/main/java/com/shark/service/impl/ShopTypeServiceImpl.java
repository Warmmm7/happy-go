package com.shark.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.entity.ShopType;
import com.shark.mapper.ShopTypeMapper;
import com.shark.service.IShopTypeService;
import org.springframework.stereotype.Service;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

}
