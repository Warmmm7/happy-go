package com.shark.service.impl;

import com.shark.entity.Blog;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.mapper.BlogMapper;
import com.shark.service.IBlogService;
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
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
