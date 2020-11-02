package com.yimian.elasticsearch.test;


import com.yimian.elasticsearch.ItemRepository;
import com.yimian.elasticsearch.pojo.Item;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;

/*    GET /item/_mapping  查询创建的映射
    GET /item             查询创建的索引        */
    @Test
    public void testIndex(){
        this.elasticsearchTemplate.createIndex(Item.class);
        this.elasticsearchTemplate.putMapping(Item.class);


    }
   @Test
    public void testCreate(){
        //    GET _search
    /*   Item item = new Item(1L, "小米手机7", " 手机",
               "小米", 3499.00, "http://image.leyou.com/13123.jpg");
       this.itemRepository.save(item);*/

       List<Item> list = new ArrayList<>();
       list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg"));
       list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg"));
       // 接收对象集合，实现批量新增
       itemRepository.saveAll(list);


    }
    @Test
    public void  testFind(){

        //   GET /item/_search
        Optional<Item> item = this.itemRepository.findById(1l);
        System.out.println(item.get());
    }

    @Test
    public void testFindAll(){
        // GET /item/_search
        Iterable<Item> items = this.itemRepository.findAll(Sort.by("price").descending());
      items.forEach(System.out::println);
      //调用  system out静态方法

    }

    //自定义方法查询
    @Test
    public  void testFindByTitle(){
        List<Item> items = this.itemRepository.findByTitle("手机");
        items.forEach(System.out::println);

    }

    @Test    //查询价格区间
    public void  testFindBetween(){
        List<Item> items = this.itemRepository.findByPriceBetween(3699d,4499d);
        items.forEach(System.out::println);
    }
    @Test
    public void indexList(){
        List<Item> list  = new ArrayList<>();
        list.add(new Item(11L, "坚果手机2", " 手机", "锤子", 1699.00, "http://image.leyou.com/123.jpg"));
        list.add(new Item(4L, "华为2", " 手机", "华为", 3399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(5L, "华为3", " 手机", "华为", 4399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(6L, "华为4", " 手机", "华为", 5399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(7L, "华为5", " 手机", "华为", 6399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(8L, "华为6", " 手机", "华为", 7399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(9L, "华为7", " 手机", "华为", 8399.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(10L, "华8", " 手机", "华为", 9399.00, "http://image.leyou.com/3.jpg"));
        itemRepository.saveAll(list);
    }


    @Test
    public void testSearch(){
        //通过查询构建器工具构建查询条件
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", "手机");
        //执行查询，获取结果集
        Iterable<Item> search = this.itemRepository.search(queryBuilder);
        search.forEach(System.out::println);
    }

    @Test
    public void testNative(){
        //构建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本的查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "手机"));
        //添加分页条件，页码是从零开始
       // queryBuilder.withPageable(PageRequest.of(1,2));
        //执行查询获取分页结果集
        Page<Item> itemPage = this.itemRepository.search(queryBuilder.build());
        System.out.println(itemPage.getTotalPages());
        System.out.println(itemPage.getTotalElements());
       itemPage.getContent().forEach(System.out::println);


    }


    @Test
    public void testPage(){
        //构建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本的查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("category", "手机"));
        //添加分页条件，页码是从零开始
       // queryBuilder.withPageable(PageRequest.of(0,2));
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        //执行查询获取分页结果集
        Page<Item> itemPage = this.itemRepository.search(queryBuilder.build());
        System.out.println(itemPage.getTotalPages());
        System.out.println(itemPage.getTotalElements());
        itemPage.getContent().forEach(System.out::println);


    }


   // GET /item/_search


/*    GET /item/_search
    {
        "size":0,
            "aggs":{
        "brandAgg":{
            "terms":{
                "field":"brand"
            }
        }
    }
    }*/



//聚合查询
    @Test
    public void testAggs(){
        //初始化自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brand"));
       //结果集过滤，不包括任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
      //执行聚合查询
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>)this.itemRepository.search(queryBuilder.build());
      //解析聚合结果集,根据聚合的类型以及 字段类型 进行强转 ；brand是字符串类型，聚合类型 词条聚合，brandAgg-通过聚合名称获取聚合对象
        StringTerms brandAgg = (StringTerms)itemPage.getAggregation("brandAgg");
        //获取桶的集合
        List<StringTerms.Bucket> buckets = brandAgg.getBuckets();
        buckets.forEach(bucket ->{
            System.out.println(bucket.getKeyAsString());
            System.out.println(bucket.getDocCount());

        });


    }


    /*GET /item/_search
    {
        "size":0,
            "aggs":{
        "brandAgg":{
            "terms":{
                "field":"brand"
            },
            "aggs":{
                "price_avg":{
                    "avg":{
                        "field":"price"
                    }
                }
            }
        }
    }
    }*/



    //子聚合

    @Test
    public void testSubAggs(){
        //初始化自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brand").subAggregation(AggregationBuilders.avg("price_avg").field("price")));
        //结果集过滤，不包括任何字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行聚合查询
        AggregatedPage<Item> itemPage = (AggregatedPage<Item>)this.itemRepository.search(queryBuilder.build());
        //解析聚合结果集,根据聚合的类型以及 字段类型 进行强转 ；brand是字符串类型，聚合类型 词条聚合，brandAgg-通过聚合名称获取聚合对象
        StringTerms brandAgg = (StringTerms)itemPage.getAggregation("brandAgg");
        //获取桶的集合
        List<StringTerms.Bucket> buckets = brandAgg.getBuckets();
        buckets.forEach(bucket ->{
            System.out.println(bucket.getKeyAsString());
            System.out.println(bucket.getDocCount());
            //获取子聚合的map集合: key-聚合名称，value对应的子聚合对象   price_avg聚合的字段解析
            Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
            InternalAvg price_avg =(InternalAvg) stringAggregationMap.get("price_avg");
            System.out.println(price_avg.getValue());
        });
    }
}
