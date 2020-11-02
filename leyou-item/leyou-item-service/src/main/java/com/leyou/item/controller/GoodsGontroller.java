package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsGontroller {

    @Autowired
    private GoodsService  goodsService;



    //http://localhost:8081/spu/page?page=1&rows=100
    //分页查询spu
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value="key",required= false) String key,
            @RequestParam(value="saleable",required= false) Boolean saleable,
            @RequestParam(value="page",defaultValue ="1" ) Integer page,
            @RequestParam(value="rows",defaultValue="5") Integer rows){
         PageResult<SpuBo> result =   this.goodsService.querySpuByPage(key,saleable,page,rows);
        if(CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);

    }

     //由于传过来的数据中包含 skus  skudetai  spu 等等  所以需要去扩展 spuBo
    //新增商品
    @PostMapping("goods")
    public  ResponseEntity<Void > saveGoods(@RequestBody SpuBo spuBo){
          this.goodsService.saveGoods(spuBo);
          return  ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //根据主键查询spuDetail   请求路径   “、/item/spu/detail/” + oldGoods.id
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
     SpuDetail spuDetail =   this.goodsService.querySpuDetailBySpuId(spuId);
     if(spuDetail == null){
         return ResponseEntity.notFound().build();
     }
     return ResponseEntity.ok(spuDetail);
    }



     //根据spuid查询所有的spu
    // “/item/sku/list?id= ” + oldGoods.id
    //@GetMapping("sku/{id}")



   // http://localhost:8081/sku/list?id=2
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long spuId ){
     List<Sku> skus = this.goodsService.querySkusBySpuId(spuId);
        if(CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }


    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
      Spu spu =  this.goodsService.querySpuById(id);
      if(spu == null){
          return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(spu);

    }
}
