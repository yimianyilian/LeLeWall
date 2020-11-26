package com.yimian.cart.controller;


import com.yimian.cart.pojo.Cart;
import com.yimian.cart.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
public class CartController {
    @Autowired
    private CartService cartService;



    //json对象使用 requestbody接收

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        if (StringUtils.isEmpty(cart)){
            return ResponseEntity.ok().build();
        }
        this.cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }
}
