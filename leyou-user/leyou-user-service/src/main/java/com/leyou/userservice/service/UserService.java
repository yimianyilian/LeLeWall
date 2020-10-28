package com.leyou.userservice.service;


import com.leyou.userservice.mapper.UserMapper;
import com.leyou.userservice.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


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
}
