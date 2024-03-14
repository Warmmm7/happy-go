package com.shark.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.Result;
import com.shark.entity.VoucherOrder;


public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);
}
