package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    //http://localhost:8081/spu/page?page=1&rows=100
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //1.添加模糊查询
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        //2.添加是否上下架的过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);

        }
        //3.添加分页
        PageHelper.startPage(page, rows);
        //4.执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        //5.把list<spu>转化成 list< spubo>
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            //把spusuo所有属性值copy给spubo
            BeanUtils.copyProperties(spu, spuBo);
            //设置品牌名称和分类名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;

        }).collect(Collectors.toList());


        //返回分页结果集
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    //事务处理
    //新增商品
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //1 新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());


        this.spuMapper.insertSelective(spuBo);


        //2 新增SPUDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        //3 新增sku
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            //4 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            this.stockMapper.insertSelective(stock);
        });
        // this.skuMapper.insertList(skus);
        sendMsg("insert",spuBo.getId());

    }


    private  void sendMsg(String type,Long id){
        try {
            this.amqpTemplate.convertAndSend("item."+ type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }
    //根据主键查询 spudetail
    public SpuDetail querySpuDetailBySpuId(Long spuId) {

        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }


    //根据spuid查询所有的spu   查询sku集合
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        skus.forEach(s -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });

        return skus;
    }

    public Spu querySpuById(Long id) {
        return  this.spuMapper.selectByPrimaryKey(id);

    }


    private void saveSkuAndStock(SpuBo spuBo){
        spuBo.getSkus().forEach( sku -> {
            //新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    @Transactional
    public void updateGoods(SpuBo spuBo){
        //查询以前sku
        List<Sku> skus = this.querySkusBySpuId(spuBo.getId());
        //如果以前存在，则删除
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            //删除以前的Sku
            Sku record = new Sku();
            record.setSpuId(spuBo.getId());
            this.skuMapper.delete(record);
        }

        //新增sku和库存
        saveSkuAndStock(spuBo);

        //更新SPU
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        //更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        this.sendMsg( "update",spuBo.getId());
    }


    public Spu querySpuBySpuId(Long id){
        return this.spuMapper.selectByPrimaryKey(id);
    }



}
