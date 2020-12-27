package com.ty.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:27
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
