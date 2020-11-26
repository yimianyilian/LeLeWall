package com.yimian.cart.interceptor;


import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.netflix.discovery.converters.Auto;
import com.yimian.cart.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//编写拦截器
@Component   //加入spring 容器  可以注入到 LeyouWebMvcConfiguration
@EnableConfigurationProperties(JwtProperties.class)


//  如果是实现  implements   handlerinterceptor  该接口 ;就需要实现三个方法，有时不需要实现所有方法 选择继承类

public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
   private JwtProperties jwtProperties;


  //private  static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();



    // 定义一个线程域，存放登录用户
 private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public LoginInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
     //获取cookie中的token
     String token =   CookieUtils.getCookieValue(request,this.jwtProperties.getCookieName());
        //解析token 获取用户信息
      UserInfo userInfo = JwtUtils.getInfoFromToken(token,this.jwtProperties.getPublicKey());
      if(userInfo ==null){
          return false;
      }
      //把userinfo 放入线程局部变量
        tl.set(userInfo);
      return true ;

        // 查询token


/*        String token = CookieUtils.getCookieValue(request, "LY_TOKEN");
        if (StringUtils.isBlank(token)) {
            // 未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        // 有token，查询用户信息
        try {
            // 解析成功，证明已经登录
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            // 放入线程域
            tl.set(user);
            return true;
        } catch (Exception e){
            // 抛出异常，证明未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }*/
    }

      public static UserInfo getUserInfo(){
          return tl.get();
        }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();
        //清空线程的局部变量，因为使用的是tomcat的线程池，线程不会结束，也就不会释放线程的局部变量

    }

    public static UserInfo getLoginUser() {
        return tl.get();
    }
}
