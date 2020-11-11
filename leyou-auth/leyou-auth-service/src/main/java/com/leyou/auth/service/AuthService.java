package com.leyou.auth.service;


import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.userservice.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    @Autowired
   private UserClient  userClient;
    @Autowired
    private JwtProperties jwtProperties;



    public String accredit(String username, String password) {
        //1.根据用户名密码查询
    User user=   this.userClient.queryUser(username,password);


        //2. 判断user
     if(user == null){
         return null;
     }

        try {
            //3.jwtUtils生成jwt类型的token
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());

       return     JwtUtils.generateToken(userInfo,this.jwtProperties.getPrivateKey(),this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
          return  null ;
    }
}
