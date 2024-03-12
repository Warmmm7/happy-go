package com.shark.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.dto.LoginFormDTO;
import com.shark.dto.Result;
import com.shark.entity.User;

import javax.servlet.http.HttpSession;


public interface IUserService extends IService<User> {

    /**
     * 发送短信验证码
     *
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 登录
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

}
