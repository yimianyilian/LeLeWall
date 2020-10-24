package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.ws.Response;
import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;




//     http://localhost:8081/category/list?pid=0
//     http://api.leyou.com/api/item/category/list?pid=0


    //根据父类目id查询查询所有子节点
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {

            if (pid == null || pid < 0) {
                //响应400   2种写法
              //  return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
               // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                return ResponseEntity.badRequest().build();
            }
            //执行查询 获取结果集
            List<Category> categories = this.categoryService.queryCategoriesByPid(pid);
            if (CollectionUtils.isEmpty(categories)){
                //响应404
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
          //  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return ResponseEntity.notFound().build();
        }
            //响应 200
            return ResponseEntity.ok(categories);

        //响应  500
          //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }
     @GetMapping
      public   ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids")List<Long> ids){
         List<String> names = this.categoryService.queryNamesByIds(ids);
         if (CollectionUtils.isEmpty(names)){
             //响应404
             //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
             //  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
             return ResponseEntity.notFound().build();
         }
         //响应 200
         return ResponseEntity.ok(names);
     }

}
