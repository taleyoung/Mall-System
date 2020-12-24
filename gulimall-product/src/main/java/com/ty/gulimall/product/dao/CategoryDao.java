package com.ty.gulimall.product.dao;

import com.ty.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:28
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
