package com.shark.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shark.dto.Result;
import com.shark.dto.UserDTO;
import com.shark.entity.Blog;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.entity.User;
import com.shark.mapper.BlogMapper;
import com.shark.service.IBlogService;
import com.shark.service.IUserService;
import com.shark.utils.SystemConstants;
import com.shark.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shark.utils.RedisConstants.BLOG_LIKED_KEY;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = getById(id);
        if(blog == null){
            return Result.fail("分享笔记不存在...");
        }
        //查询Blog有关的用户
        queryBlogUser(blog);
        //查询blog是否被点赞....
        isBlogLiked(blog);
        return Result.ok(blog);
    }


    /**
     * 热点查询
     * @param current
     * @return
     */
    @Override
    public Result queryHotBlog(Integer current) {

        // 根据用户查询
        Page<Blog> page =query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
           this.queryBlogUser(blog);
           this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    /**
     * 点赞逻辑
     * @param id
     * @return
     */
    @Override
    public Result likeBlog(Long id) {
        //先获取用户 判断当前用户是否点赞...
        Long userId = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + id;
        Double score  = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            //没点赞 可以点赞
            //数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked +1").eq("id", id).update();
            //保存用户到redis的set集合
            if(isSuccess){//保存用户到Redis的set集合 zadd key value score
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else {
            //已经点赞 取消点赞
            boolean isSuccess = update().setSql("liked = liked -1").eq("id", id).update();
            stringRedisTemplate.opsForZSet().remove(key,userId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY+id;
        //查询top点赞用户 zrange key 0 4
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if(top5 == null || top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //解析用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        //根据用户id查询用户
        List<UserDTO> userDTOs = userService.query().in("id",ids)
                .last("ORDER BY FIELD(id,"+ idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        //返回...
        return Result.ok(userDTOs);
    }

    //常用方法
    private void queryBlogUser(Blog blog){
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return;//用户没登陆 不能查询点赞...
        }
        Long userId = user.getId();
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);//已经点过赞的..
    }
}
