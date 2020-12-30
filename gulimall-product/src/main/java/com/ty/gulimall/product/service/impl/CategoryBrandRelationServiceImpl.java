package com.ty.gulimall.product.service.impl;

import com.ty.gulimall.product.dao.BrandDao;
import com.ty.gulimall.product.dao.CategoryDao;
import com.ty.gulimall.product.entity.BrandEntity;
import com.ty.gulimall.product.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.CategoryBrandRelationDao;
import com.ty.gulimall.product.entity.CategoryBrandRelationEntity;
import com.ty.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelationEntity){
        Long catelogId = categoryBrandRelationEntity.getCatelogId();
        Long brandId = categoryBrandRelationEntity.getBrandId();

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelationEntity.setBrandName(brandEntity.getName());
        categoryBrandRelationEntity.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelationEntity);
    }

}