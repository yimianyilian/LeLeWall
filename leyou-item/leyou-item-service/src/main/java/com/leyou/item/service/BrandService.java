package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    //分页查询品牌
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
       //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria= example.createCriteria();

        //添加模糊查询
       if(StringUtils.isNotBlank(key)){
           criteria.andLike("name" ,"%" + key + "%").orEqualTo("letter",key);

       }

        //添加分页
        PageHelper.startPage(page,rows);

        //添加排序

        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + (desc ? "desc" :" asc"));
        }
         //找到适合的通用mapper方法
        List<Brand> brands = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult(pageInfo.getTotal(),pageInfo.getList());
    }
   //加事务  确保完全提交
    @Transactional
    //新增品牌
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
      this.brandMapper.insertSelective(brand);
      cids.forEach(cid ->
          this.brandMapper.saveCategoryAndBrand(cid,brand.getId())
      );

    }

    //通用mapper只能处理单表
    //根据分类ID查询品牌列表
    //通过中间表查询
    public List<Brand> queryBrandsByCid(Long cid) {


    return  this.brandMapper.selectByCid(cid);


    }

    public Brand queryBrandsById(Long id) {
        return  this.brandMapper.selectByPrimaryKey(id);

    }
}
