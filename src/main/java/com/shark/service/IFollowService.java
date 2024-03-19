package com.shark.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.Result;
import com.shark.entity.Follow;

public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result followCommons(Long id);
}
