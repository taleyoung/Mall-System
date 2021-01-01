package com.ty.gulimall.product.service.impl;

import com.ty.gulimall.product.entity.CategoryBrandRelationEntity;
import com.ty.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.CategoryDao;
import com.ty.gulimall.product.entity.CategoryEntity;
import com.ty.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import sun.jvm.hotspot.debugger.Page;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    public List<CategoryEntity> listWithTree(){
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> level1 = list.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid()==0)
                .peek(entity -> entity.setChildren(getChildren(entity, list)))
                .sorted((menu1, menu2)->{
                    Integer menu1Sort = menu1.getSort() == null ? 0 : menu1.getSort();
                    Integer menu2Sort = menu2.getSort() == null ? 0 : menu2.getSort();
                    return menu1Sort - menu2Sort;
                })
                .collect(Collectors.toList());

        return level1;
    }

    public void removeMenuByIds(List<Long> asList){
        //TODO: 检查当前删除的菜单，是否被别的地方引用。
        baseMapper.deleteBatchIds(asList);
    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek(entity-> entity.setChildren(getChildren(entity, all)))
                .sorted((menu1, menu2)->{
                    Integer menu1Sort = menu1.getSort() == null ? 0 : menu1.getSort();
                    Integer menu2Sort = menu2.getSort() == null ? 0 : menu2.getSort();
                    return menu1Sort - menu2Sort;
                })
                .collect(Collectors.toList());
        return children;
    }

    //拿到三级分类的完整路径
    public Long[] findCatelogPath(Long categoryId){
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(categoryId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联数据
     */
    @Transactional
    public void updateCascade(CategoryEntity categoryEntity){
        this.updateById(categoryEntity);
        categoryBrandRelationService.updateCategory(categoryEntity.getCatId(), categoryEntity.getName());
    }

    public List<Long> findParentPath(Long categoryId, List<Long> paths){
        paths.add(categoryId);
        CategoryEntity byId = this.getById(categoryId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }
}