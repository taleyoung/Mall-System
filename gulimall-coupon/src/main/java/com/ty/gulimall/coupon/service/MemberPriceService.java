package com.ty.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 22:47:57
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

