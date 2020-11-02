package com.yimian.elasticsearch;

import com.yimian.elasticsearch.pojo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

//item 是需要管理类型     long主键
public interface ItemRepository extends ElasticsearchRepository<Item,Long> {
    //自定义查询方法
    List<Item> findByTitle(String title);
    //查询价格区间
    List<Item> findByPriceBetween(Double d1,Double d2);
}
