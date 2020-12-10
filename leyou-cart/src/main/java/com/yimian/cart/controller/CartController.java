package com.yimian.cart.controller;


import com.yimian.cart.pojo.Cart;
import com.yimian.cart.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.ws.Response;
import java.util.List;


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


/*     this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();*/

    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts(){
      List<Cart> carts=  this.cartService.queryCarts();
      if(CollectionUtils.isEmpty(carts)){
          return ResponseEntity.notFound().build();
      }
       return ResponseEntity.ok(carts);
    }



    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseEntity.noContent().build();
    }

}
