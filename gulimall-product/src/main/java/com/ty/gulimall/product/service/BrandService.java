package com.ty.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:28
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

