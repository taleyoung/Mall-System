package com.ty.gulimall.product.service.impl;

import com.ty.common.constant.ProductConstant;
import com.ty.common.to.SkuHasStockVo;
import com.ty.common.to.SkuReductionTo;
import com.ty.common.to.SpuBoundTo;
import com.ty.common.to.es.SkuEsModel;
import com.ty.common.utils.R;
import com.ty.gulimall.product.entity.*;
import com.ty.gulimall.product.feign.CouponFeignService;
import com.ty.gulimall.product.feign.SearchFeignService;
import com.ty.gulimall.product.feign.WareFeignService;
import com.ty.gulimall.product.service.*;
import com.ty.gulimall.product.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    AttrService attrService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    SkuInfoServiceImpl skuInfoService;

    @Autowired
    SkuImagesServiceImpl skuImagesService;

    @Autowired
    ProductAttrValueServiceImpl productAttrValueService;

    @Autowired
    SkuSaleAttrValueServiceImpl skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1.保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //2.保存spu的描述图片  pms_spu_info_desc
        List<String> descript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",", descript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);


        //3.保存spu的图片集   pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuId, images);

        //4.保存spu的规格参数  pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        productAttrValueService.saveBaseAttrs(spuId, baseAttrs);

        //远程rpc调用 保存spu的积分信息  gulimall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        R saveSpuBoundsR  = couponFeignService.saveSpuBounds(spuBoundTo);
        if(saveSpuBoundsR.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        //5.保存当前对spu对应的所有sku信息
            //5.1 sku的基本信息 sku_info
        List<Skus> skus = vo.getSkus();
        if(skus!=null && skus.size() > 0){
            skus.forEach(sku->{
                String defaultImg = "";
                for(Images image : sku.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //5.2 sku的图片信息   sku_images
                List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity-> !StringUtils.isEmpty(entity.getImgUrl()))
                        .collect(Collectors.toList());
                //TODO: 空白的url
                skuImagesService.saveBatch(imagesEntities);

                //5.3 sku的销售属性信息    sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> attrValueEntities = attr.stream().map(item -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);
                    return saleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(attrValueEntities);

                //5.4 sku的优惠信息 gulimall_sms -> sms_sku_ladder
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0){
                    R saveSkuR = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(saveSkuR.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });


        }




    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<SpuInfoEntity>();

        String key =(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status =(String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status", status);
        }
        String brandId =(String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        String catelogId =(String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id", catelogId);
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 商品上架功能
     * @param spuId
     */
    @Override
    public void up(Long spuId) {
        //1.组装需要的数据
        //1.查出当前spiId对应的所有sku信息，品牌的名字。
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        //2)hasStock,  发送远程调用 库存系统查询是否有库存
        // 提到循环外面来get 循环内set
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try{
            List<SkuHasStockVo> skuHasStock = wareFeignService.getSkuHasStock(skuIds);
            stockMap = skuHasStock.stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常", e);
        }



        //5) 查出sku所有可以被检索的规格属性 即attrs 有spu控制，故提到循环外面来做。
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> baseAttrIds = baseAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(baseAttrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> esAttrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item->{
                    SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs);
                    return attrs;
                })
                .collect(Collectors.toList());


        //分装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            //接着对比sku和skuEsModel中不一样的数据类型，要分别进行处理
            //1)skuPrice skuImg
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            //2)hasStock,  发送远程调用 库存系统查询是否有库存
            if(finalStockMap == null){
                skuEsModel.setHasStock(true);
            }else{
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }




            //3) hotScore热度评分：暂时为0
            skuEsModel.setHotScore(0L);

            //4)brandName catalogName
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            //5) 查出sku所有可以被检索的规格属性 即attrs 有spu控制，故提到循环外面来做。
            //esAttrsList 在循环外做好
            skuEsModel.setAttrs(esAttrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        //5. 将数据发送给es进行保存  gulimall-search
        R r = searchFeignService.productStatusUp(skuEsModels);
        if(r.getCode() == 0){
            //6. 修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //重复调用？接口幂等性。重试机制？
        }
    }


}