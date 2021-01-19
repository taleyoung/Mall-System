package com.ty.gulimall.product.vo;

import com.ty.gulimall.product.entity.SkuImagesEntity;
import com.ty.gulimall.product.entity.SkuInfoEntity;
import com.ty.gulimall.product.entity.SpuInfoDescEntity;
import com.ty.gulimall.product.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //1. sku基本信息获取 pms_sku_info
    SkuInfoEntity info;

    //2. sku的图片信息  pms_sku_images
    List<SkuImagesEntity> images;

    //3. 获取spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;

    //4. 获取spu介绍
    SpuInfoDescEntity desp;

    //5. 获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

}
