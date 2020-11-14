package com.leyou.auth.controller;


import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.utils.CookieUtils;
import com.netflix.discovery.converters.Auto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;


    //http://api.leyou.com/api/auth/accredit?username=liuyan&password=123456
    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(@RequestParam("username")String username,
                                         @RequestParam("password")String password,
                                         HttpServletRequest request,
                                         HttpServletResponse response){
        String token =   this.authService.accredit(username,password);
       // System.out.println("token:"+token);
      if(StringUtils.isBlank(token)){
          //401  httpcode
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

        CookieUtils.setCookie(request,response,this.jwtProperties.getCookieName(),token,this.jwtProperties.getExpire()*60);
       return  ResponseEntity.ok(null);

    }
}
