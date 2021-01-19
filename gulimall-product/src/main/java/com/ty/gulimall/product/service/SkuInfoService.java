package com.ty.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.product.entity.SkuInfoEntity;
import com.ty.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:28
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity infoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException;
}

