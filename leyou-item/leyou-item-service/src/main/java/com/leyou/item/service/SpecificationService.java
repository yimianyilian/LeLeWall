package com.leyou.item.service;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
   private SpecGroupMapper  specGroupMapper;
    @Autowired
   private SpecParamMapper specParamMapper;

    //根据分类ID查询规格参数组
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
       return  this.specGroupMapper.select(record);

    }

    //根据条件查询 规格参数
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam  record  = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);

        return this.specParamMapper.select(record);
    }

    public List<SpecGroup> queryGroupsWithParam(Long cid) {

        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(group ->{
            List<SpecParam> params = this.queryParams(group.getId(), null, null, null);
             group.setParams(params);
        });
        return groups;
    }
}
