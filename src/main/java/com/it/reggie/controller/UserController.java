package com.it.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.it.reggie.common.R;
import com.it.reggie.entity.User;
import com.it.reggie.service.UserService;
//import com.it.reggie.utils.SMSUtils;
import com.it.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if (!StringUtils.isEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用阿里云提供的短信服务api发送短信
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            //需要将生成的验证码保存到Session
            session.setAttribute(phone, code);

            return R.success("手机验证码发送成功");

        }
        return R.error("手机验证码发送失败");




    }
    /**
     * 移动端用户登录
     * @param map
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession != null&&codeInSession.equals(code)){
            //如果能够比对成功，说明登陆成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //判断当前手机号对应用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }


        return R.error("登录失败");

    }

    /**
     * 用户退出登录
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}
