package com.shark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.Result;
import com.shark.entity.Blog;


public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);
}
