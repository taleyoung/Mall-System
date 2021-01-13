package com.ty.gulimall.search.controller;


import com.ty.common.exception.BizCodeEnume;
import com.ty.common.to.es.SkuEsModel;
import com.ty.common.utils.R;
import com.ty.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search")
@RestController
public class ElasticSaveController {
    @Autowired
    ProductSaveService productSaveService;

    /**
     * 商品上架
     * @return
     */
    @PostMapping("save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModelList);
        }catch (IOException e){
            log.error("es商品上架错误{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(b){
            return R.ok();
        }else{
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

    }
}
