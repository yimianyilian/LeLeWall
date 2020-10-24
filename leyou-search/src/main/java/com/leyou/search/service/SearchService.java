package com.leyou.search.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.repository.GoodsRepository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.netflix.discovery.converters.Auto;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private CategoryClient  categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
       //根据分类的ID查询分类名称
     List<String> names =  this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
       //根据品牌ID查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spuid查询所有的sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //初始化一个价格集合，收集所有sku的价格
        List<Long> prices = new ArrayList<>();
        //收集sku的必要字段信息
        List<Map<String,Object>>  skuMapList =   new ArrayList<>();

        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            //获取sku中的图片，数据库的图片可能是多张，多张是以“，”分隔，所以也以逗号来切割返回图片数组，获取第一张图片
            map.put("image",StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuMapList.add(map);
        });
        //根据spu中的cid3查询所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null,spu.getCid3(),null,true);
        //根据spuid查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
       //反序列化  把通用的规格参数值  进行反序列化  ：：  将json 转换为字符串  第一个参数你需要反序列谁   第二参数 反序列化的类型
     Map<String,Object> genericSpecMap =   MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<String,Object>>(){});
     //把特殊的规格参数,进行反序列化

     Map<String,List<Object>> specialSpecMap  =   MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<String,List<Object>>>(){});

     Map<String ,Object> specs = new  HashMap<>();

     params.forEach(param ->{
         //判断规格参数的类型  是否是通用的规格参数
     if(param.getGeneric()){
         //如果是通用类型的参数，从genericSpecMap获取规格参数值
        String  value = genericSpecMap.get(param.getId().toString()).toString();
        //判断是否是数值类型，如果是数值类型，应该返回一个区间
        if(param.getNumeric()){
           value = chooseSegment(value,param);
        }
        specs.put(param.getName(),value);
     }else{
         //如果是特殊的规格参数，从specialspcmap中获取值
         List<Object> value = specialSpecMap.get(param.getId().toString());
         specs.put(param.getName(),value);
     }
     });

        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //拼接all字段  需要分类名称以及品牌名称        name里面自己加入 空格作为分隔符
        goods.setAll(spu.getTitle()+" "+ StringUtils.join(names," ") + " " + brand.getName());
        //获取spu下所有sku的价格
        goods.setPrice(prices);
        //获取spu下所有sku  并转化为json字符串   序列化为json    readvalue反序列化
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //获取所有查询的规格参数 [name,value]
        goods.setSpecs(specs);

        return goods;

    }

//        (1.0-1.1 ,1.5-2.0,2.1-3.0 ,3.0)
    private String chooseSegment(String value, SpecParam p) {
        //什么数都可以转为double  为了更好通用
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

/*
 网页最终获取到的json数据  ：

 brands: [{id: 8557, name: "华为（HUAWEI）",…}, {id: 18374, name: "小米（MI）",…}, {id: 12669, name: "魅族（MEIZU）",…},…]
            0: {id: 8557, name: "华为（HUAWEI）",…}
    id: 8557
    image: "http://img10.360buyimg.com/popshop/jfs/t5662/36/8888655583/7806/1c629c01/598033b4Nd6055897.jpg"
    letter: "H"
    name: "华为（HUAWEI）"
=======================================================================================
    categories: [{name: "手机", id: 76}]
            0: {name: "手机", id: 76}
    id: 76
    name: "手机"

    items: [{id: 57,…}, {id: 88,…}, {id: 92,…}, {id: 141,…}, {id: 145,…}, {id: 164,…}, {id: 124,…}, {id: 147,…},…]
            0: {id: 57,…}
    id: 57
    skus: "[{"image":"http://image.leyou.com/images/4/11/1524297413085.jpg","price":79900,"id":3924115,"title":"360手机 F4S 3GB+32GB 星空灰 移动定制版 移动联通电信4G手机 双卡双待"},{"image":"http://image.leyou.com/images/5/6/1524297412607.jpg","price":37900,"id":5568865,"title":"360手机 F5 移动版 2GB+16GB 流光金 移动联通4G手机 双卡双待"}]"
    subTitle: "2G+16G内存/5英寸屏幕/2.5D弧形玻璃/前置指纹识别<a href='http://sale.jd.com/act/lhyRYJDSTC.html' target='_blank'>360手机春季惠场，爆品领券立减100>></a>"
====================================================================================

    specs: [{k: "后置摄像头", option: ["1000-1500万", "500万以下", "1500-2000万", "500-1000万", "其它"]},…]
            0: {k: "后置摄像头", option: ["1000-1500万", "500万以下", "1500-2000万", "500-1000万", "其它"]}
    k: "后置摄像头"
    option: ["1000-1500万", "500万以下", "1500-2000万", "500-1000万", "其它"]
            0: "1000-1500万"
            1: "500万以下"
            2: "1500-2000万"
            3: "500-1000万"
            4: "其它"*/





   // public PageResult<Goods> search(SearchRequest request) {
   public SearchResult search(SearchRequest request) {
        if(StringUtils.isBlank(request.getKey())){
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件   可能输入一个关键字 也可能输入两个关键字   所以要用匹配查询 ； 搜索 小米手机不能把小米电视查询出来  and关系
      //  queryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));

       // QueryBuilder basicQuery = QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND);
      BoolQueryBuilder basticQuery =   buildBoolQueryBuilder(request);


       queryBuilder.withQuery(basticQuery);


       //添加分页
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
         //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null ));

        //添加分类和品牌的聚合
       String categoryAggName ="categories";
       String  brandAggName ="brands";

       queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
       queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //执行查询，获取结果集
       AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)  this.goodsRepository.search(queryBuilder.build());


       //获取聚合结果集并解析
      List<Map<String,Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
      List<Brand> brands = getBrandAggResult(goodsPage.getAggregation(brandAggName));

       List<Map<String,Object>> specs =null ;
      //判断是否是一个分类，只有一个分类时才做规格参数的聚合
       if(  !CollectionUtils.isEmpty(categories) && categories.size()== 1   ){
           //对规格参数进行聚合
         specs =    getParamAggResult((Long)categories.get(0).get("id"),basticQuery);

       }


        return  new SearchResult(goodsPage.getTotalElements() , goodsPage.getTotalPages(), goodsPage.getContent() ,categories,  brands,specs);


    }





//========================================================================

    /*   过滤结果集  ：首先要是一个boolean  查询 ； 查询完之后 filter过滤

GET  /goods/_search
        {
        "query":{
        "bool":{
        "must":[
        {
        "match":{
        "all":"手机"
        }
        }
        ],
        "filter":{
        "term":{
        "specs.CPU核数.keyword": "十核"
        }
        }
        }

        }
        }*/

    //构建布尔查询   里面要放基本查询条件  跟过滤条件

    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //给布尔查询添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加过滤条件
        //获取用户选择的过滤信息
        Map<String,Object> filter =    request.getFilter();

        //遍历map 通过entryset
        for(Map.Entry<String,Object> entry : filter.entrySet()){
            String key = entry.getKey();
            if(StringUtils.equals("品牌",key)){
                key="brandId";
            }else if(StringUtils.equals("分类",key)){
                key= "cid3";

            }else{
                key="specs." + key + ".keyword";
            }
             boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue() ));
        }
        return  boolQueryBuilder;

    }

//====================================================================
/*
    GET /goods/_search
    {
        "size" :0 ,
            "aggs" :{
        "CPU核数" :{
            "terms" :{
                "field" :"specs.CPU核数.keyword"
            }

        }

    }
    }*/



    /*"aggregations": {
        "CPU核数": {
            "doc_count_error_upper_bound": 0,
                    "sum_other_doc_count": 0,
                    "buckets": [
            {
                "key": "八核",
                    "doc_count": 142
            },
            {
                "key": "四核",
                    "doc_count": 20
            },
            {
                "key": "其他",
                    "doc_count": 10
            },
            {
                "key": "十核",
                    "doc_count": 3
            },
            {
                "key": "八核 + 微智核i6",
                    "doc_count": 2
            },
            {
                "key": "六核",
                    "doc_count": 2
            },
            {
                "key": "四核+四核",
                    "doc_count": 2
            },
            {
                "key": "CPU核数八核 + 微智核i7",
                    "doc_count": 1
            }
      ]
        }
    }*/


    //根据查询条件聚合规格参数
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
       //自定义查询对象构建
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询条件
        queryBuilder.withQuery(basicQuery);

        //查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);
        //添加规格参数的聚合
        params.forEach( param ->{
           //terms 肯定是词条
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName() +".keyword"));
       });
        //添加结果集过滤
         queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
         
        //执行聚合查询  获取聚合结果集
       AggregatedPage<Goods>  goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

       List<Map<String,Object>> specs = new ArrayList<>();

       //解析聚合结果集 key-聚合名称（规格参数名） value-聚合对象
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
       for(Map.Entry<String,Aggregation> entry:aggregationMap.entrySet()){

             Map<String,Object> map = new HashMap<>();
             map.put("k",entry.getKey());
             //初始化一个options集合，收集桶中的key
           List<String> options = new ArrayList<>();

             //获取聚合
             StringTerms terms = (StringTerms)entry.getValue();
            //获取桶集合
           terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
           });
             map.put("option",options);
             specs.add(map);



       }

        return specs;
    }


    //解析品牌的聚合结果集


    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        LongTerms terms =(LongTerms)aggregation;
        List<Brand> brands = new ArrayList<>();
        //获取聚合中的桶
        terms.getBuckets().forEach(bucket ->{

          Brand brand =  this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
          brands.add(brand);
        });
        return  brands;


/*        return terms.getBuckets().stream().map(bucket ->{
            return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());

        }).collect(Collectors.toList());*/
    }

    //解析分类的聚合结果集
/*    GET /goods/_search
    {
        "size" :0 ,
            "aggs" :{
        "brands" :{

            "terms" :{
                "field" :"brandId"

            }

        }

    }

    }

    GET /goods/_search
    {
        "size" :0 ,
            "aggs" :{
        "categories" :{

            "terms" :{
                "field" :"cid3"

            }

        }

    }

    }*/
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
       LongTerms terms =(LongTerms)aggregation;
       //获取桶的集合 转化成List<Map<String ,Object>>
       return    terms.getBuckets().stream().map(bucket ->{
           //初始化一个map
           Map<String,Object> map= new HashMap<>();
           //获取桶中的分类Id(key)
           Long id = bucket.getKeyAsNumber().longValue();
           //根据分类ID查询分类名称
           List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(id));
           map.put("id",id);
           map.put("name",names.get(0));
           return map;
       }).collect(Collectors.toList());

    }
}


