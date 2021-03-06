package com.ty.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ty.gulimall.product.entity.AttrEntity;
import com.ty.gulimall.product.service.AttrAttrgroupRelationService;
import com.ty.gulimall.product.service.AttrService;
import com.ty.gulimall.product.service.CategoryService;
import com.ty.gulimall.product.vo.AttrGroupRelationVo;
import com.ty.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ty.gulimall.product.entity.AttrGroupEntity;
import com.ty.gulimall.product.service.AttrGroupService;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.R;



/**
 * 属性分组
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 21:37:28
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 属性分组下的包含属性
     * @return
     */
    @GetMapping("{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> entityList = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", entityList);
    }

    /**
     * 属性分组下没包含的属性
     */
    @GetMapping("{attrGroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrGroupId") Long attrGroupId,
                            @RequestParam  Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(params, attrGroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("categoryId") Long categoryId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, categoryId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

		Long  categoryId = attrGroup.getCatelogId();
		Long[] path = categoryService.findCatelogPath(categoryId);

		attrGroup.setCategoryPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 获取分类下的所有分组以及分组内的属性
     */
    @GetMapping("{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithAttrsByCatId(catelogId);
        return R.ok().put("data",attrGroupWithAttrsVos);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /*
     * 新增关联关系
     */
    @PostMapping("attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.saveBatchs(vos);
        return R.ok();
    }

    /**
     * 删除分组下的关联属性
     */
    @PostMapping("attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
        attrService.deleteRelation(attrGroupRelationVos);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
