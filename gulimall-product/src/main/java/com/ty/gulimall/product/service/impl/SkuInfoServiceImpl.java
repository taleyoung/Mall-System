package com.ty.gulimall.product.service.impl;

import com.ty.gulimall.product.entity.SkuImagesEntity;
import com.ty.gulimall.product.entity.SpuInfoDescEntity;
import com.ty.gulimall.product.service.*;
import com.ty.gulimall.product.vo.SkuItemSaleAttrVo;
import com.ty.gulimall.product.vo.SkuItemVo;
import com.ty.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.SkuInfoDao;
import com.ty.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

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

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(()->{
            //1. sku基本信息获取 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        },executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //3. 获取spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSkuSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrs);
        });

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(res -> {
            //4. 获取spu介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuInfoDescEntity);
        });

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync(res -> {
            //5. 获取spu的规格参数信息
            List<SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVos);
        });

        CompletableFuture.runAsync(()->{
            //2. sku的图片信息  pms_sku_images
            List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        },executor);

        //等待所有任务都完成
        CompletableFuture.allOf( saleAttrFuture, baseAttrFuture, descFuture).get();

        return skuItemVo;
    }


}