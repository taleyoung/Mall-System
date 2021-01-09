package com.ty.gulimall.product.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.SkuInfoDao;
import com.ty.gulimall.product.entity.SkuInfoEntity;
import com.ty.gulimall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<SkuInfoEntity>();

        String key =(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String brandId =(String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        String catelogId =(String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id", catelogId);
        }
        String min =(String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            BigDecimal bigDecimal = new BigDecimal(min);
            if(bigDecimal.compareTo(new BigDecimal("0")) > 0){
                wrapper.ge("price", min);
            }

        }
        String max =(String) params.get("max");
        if(!StringUtils.isEmpty(max) && !"0".equalsIgnoreCase(max)){
            BigDecimal bigDecimal = new BigDecimal(max);
            if(bigDecimal.compareTo(new BigDecimal("0")) > 0){
                wrapper.le("price", max);
            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}