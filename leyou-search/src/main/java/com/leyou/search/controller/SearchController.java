package com.leyou.search.controller;


import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class SearchController {
    @Autowired
    private SearchService searchService;

    //requestbody  捕捉  前端发过来对象 ，json格式的请求数据   {key="xxxxxxx" }
    @PostMapping("page")
   // public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request ){
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest request ){
      // PageResult<Goods> result =  this.searchService.search(request);
        SearchResult result =  this.searchService.search(request);
       if(result == null || CollectionUtils.isEmpty(result.getItems())){
               return  ResponseEntity.notFound().build();
        }
       return   ResponseEntity.ok(result);

    }


}
