package com.leyou.userservice.service;


import com.leyou.common.utils.NumberUtils;
import com.leyou.userservice.mapper.UserMapper;
import com.leyou.userservice.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PERFIX ="user:verify:";


    //校验数据是否可用
    public Boolean checkUser(String data, Integer type) {
        User record = new User();
        if(type== 1){
            record.setUsername(data);
        }else if(type ==2){
            record.setPhone(data);
        }else{
            return null;
        }

       return  this.userMapper.selectCount(record) ==0 ;
    }

    public void sendVerifyCode(String phone) {
        //生成验证码
        if(StringUtils.isBlank(phone)){
            return ;
        }
        String code = NumberUtils.generateCode(6);


        //发送消息到rabbitmq
        Map<String,String> msg  = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);

        this.amqpTemplate.convertAndSend("leyou.sms.exchange","verify.code.sms",msg);

        //把验证码保存到redis中
        this.redisTemplate.opsForValue().set(KEY_PERFIX +phone,code,5, TimeUnit.MINUTES);


    }
}
