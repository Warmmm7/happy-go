package com.shark.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shark.dto.LoginFormDTO;
import com.shark.dto.Result;
import com.shark.dto.UserDTO;
import com.shark.entity.User;
import com.shark.mapper.UserMapper;
import com.shark.service.IUserService;
import com.shark.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shark.utils.RedisConstants.*;
import static com.shark.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 验证码发送
     *
     * @param phone
     * @param session
     * @return
     */

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            //不符合就返回错误信息
            return Result.fail("手机号格式错误");
        }
        //符合就先生成一个验证码
        String code = RandomUtil.randomNumbers(6);
        //保存一个验证码  用redis存储 //设置个有效期
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //发送验证码
        log.debug("发送短信验证码成功...{}",code);//没用三方软件
        return Result.ok();
    }

    /**
     * y用户登录
     * @param loginForm
     * @param session
     * @return
     */
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //校验手机号 再校验验证码
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //从redis获取验证码 校验
        String  cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)){
            //不一致 报错
            return Result.fail("验证码错误！");
        }
        //一致 根据手机号查询用户 select * from user where phone = #{。。。}
        User user = query().eq("phone", phone).one();
        //判断用户是否存在
        if(user == null){
            //不存在 创建新的用户 保存到redis
            user = createUserWithPhone(phone);
        }
        //保存用户信息到redis....
        // 生成token 作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //将User对象转为hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue) -> fieldValue.toString()));

        String tokenKey = LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //设置token有效期并返回
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {

        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomString(8));
        save(user);
        return user;
    }
}
