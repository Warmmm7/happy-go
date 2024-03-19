package com.shark.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shark.dto.Result;
import com.shark.dto.UserDTO;
import com.shark.entity.Follow;
import com.shark.entity.User;
import com.shark.mapper.FollowMapper;
import com.shark.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.service.IUserService;
import com.shark.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shark.utils.RedisConstants.FOLLOW_KEY;


@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplatel;
    @Resource
    private IUserService userService;
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //先获取用户
        Long userId = UserHolder.getUser().getId();
        String key = FOLLOW_KEY+userId;
        //判断到底是关注还是取关
        if(isFollow){
            //关注 新增数据....
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if(isSuccess){
                //关注的用户 放到redis是set集合
                stringRedisTemplatel.opsForSet().add(key,followUserId.toString());
            }
        }else {
            //取关 delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));

            if(isSuccess){//移除用户 从redis集合
                stringRedisTemplatel.opsForSet().remove(key,followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        //是否关注 有用户....
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        return Result.ok(count>0);
    }

    @Override
    public Result followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key =FOLLOW_KEY+userId;

        //当前用户和目标用户求交集
        String key2 = FOLLOW_KEY+id;
        Set<String> intersect = stringRedisTemplatel.opsForSet().intersect(key, key2);
        if(intersect == null || intersect.isEmpty()){
            return Result.ok(Collections.emptyList());//没有就返回空的
        }
        //解析set
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOS = userService.listByIds(ids).stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);

    }
}
