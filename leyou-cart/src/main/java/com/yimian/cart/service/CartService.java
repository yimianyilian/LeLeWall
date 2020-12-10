package com.yimian.cart.service;


import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import com.yimian.cart.client.GoodsClient;
import com.yimian.cart.interceptor.LoginInterceptor;
import com.yimian.cart.pojo.Cart;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
   @Autowired
    private StringRedisTemplate  redisTemplate;
   @Autowired
   private GoodsClient goodsClient;

    private static final String KEY_PREFIX ="user:cart:" ;

/*    public void addCart(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println("userInfo:"+ userInfo);


        //查询购物车记录
        BoundHashOperations<String, Object, Object>  hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        String key = cart.getSkuId().toString();
        Integer  num  = cart.getNum();

        //判断当前的商品是否在购物车中
      if (hashOperations.hasKey(key)){
          // 在  更新数量
       String cartJson  = hashOperations.get(key).toString();
       cart=   JsonUtils.parse(cartJson,Cart.class);
       cart.setNum(cart.getNum() + num  );
          System.out.println("haskey1");
       //hashOperations.put(key,JsonUtils.serialize(cart));
      } else {
          //不在   新增购物车
          Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
          cart.setUserId(userInfo.getId());
          cart.setTitle(sku.getTitle());
          cart.setOwnSpec(sku.getOwnSpec());
          cart.setImage(StringUtils.isBlank(sku.getImages())?"":StringUtils.split(sku.getImages(),",")[0]);
          cart.setPrice(sku.getPrice());
          System.out.println("haskey2");


      }
        hashOperations.put(key,JsonUtils.serialize(cart));

    }*/



    public void addCart(Cart cart){

        //获取登陆用户   这边拦截器的作用只是为了获取用户信息
        UserInfo user = LoginInterceptor.getLoginUser();
        //redis的key
        String key = KEY_PREFIX + user.getId();
        //获取hash对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        //查询是否存在
        Long skuId= cart.getSkuId();
        Integer num = cart.getNum();
        Boolean bool = hashOps.hasKey(skuId.toString());
        if (bool){
            //存在，获取购物车数据
            String json = hashOps.get(skuId.toString()).toString();
            cart = JsonUtils.parse(json, Cart.class);//反序列化
            //修改购物车数据
            cart.setNum(cart.getNum() + num);
        }else {
            //不存在，新增购物车数据
            cart.setUserId(user.getId());
            //其它商品信息，需要查询商品服务
            Sku sku = this.goodsClient.querySkuBySkuId(skuId);
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" :
                    StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }
        //将购物车数据写入redis
        hashOps.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }


    public List<Cart> queryCarts() {
        //获取用户信息
      UserInfo userInfo =LoginInterceptor.getUserInfo();

      //判断用户是否有购物车记录
      if(!this.redisTemplate.hasKey(KEY_PREFIX+ userInfo.getId())){
          return null;
      }
      //获取用户的购物车记录
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

       //获取购物车MAP中所有cart值集合
        List<Object> cartsJson    = hashOps.values();
        //如果购物车集合为空，直接返回null
       if(CollectionUtils.isEmpty(cartsJson)){
           return null;
       }

        //把list<Object>集合转化为list<cart>集合
        return    cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(),Cart.class)).collect(Collectors.toList());

    }

    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo userInfo =LoginInterceptor.getUserInfo();

        //判断用户是否有购物车记录
        if(!this.redisTemplate.hasKey(KEY_PREFIX+ userInfo.getId())){
            return ;
        }

        Integer num = cart.getNum();

        //获取用户的购物车记录
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
        cart  = JsonUtils.parse(cartJson,Cart.class);
        cart.setNum(num);

        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));

    }
}
