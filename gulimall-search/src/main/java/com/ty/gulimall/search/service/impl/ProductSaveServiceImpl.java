package com.ty.gulimall.search.service.impl;

import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSON;
import com.ty.common.to.es.SkuEsModel;
import com.ty.gulimall.search.GulimallSearchApplication;
import com.ty.gulimall.search.config.GulimallESConfig;
import com.ty.gulimall.search.constant.EsConstant;
import com.ty.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service("ProductSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
        //保存到es
        //1. 在es中建立索引 product 建立映射关系

        //2.给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for(SkuEsModel model: skuEsModelList){
            //1. 构造保存请求 Index
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.type("doc");
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallESConfig.COMMON_OPTIONS);
        //TODO: 如果批量错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> item.getId()).collect(Collectors.toList());
        log.info("商品上架成功");
        return !b;
    }
}
