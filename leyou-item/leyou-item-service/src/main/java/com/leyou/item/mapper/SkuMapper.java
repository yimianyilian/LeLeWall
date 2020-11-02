package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

//InsertListMapper  一次性可以新增多条记录
public interface SkuMapper extends Mapper<Sku> , InsertListMapper<Sku> {
//public interface SkuMapper extends Mapper<Sku> {

    /**
     * 根据SpuId查询sku
     * @param id
     * @return
     */
  //  @Select("SELECT id FROM tb_sku WHERE spu_id = #{id}")
  //  List<Long> selectBySpuId(Long id);
}
