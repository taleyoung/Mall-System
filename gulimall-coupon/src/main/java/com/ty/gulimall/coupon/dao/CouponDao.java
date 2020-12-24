package com.ty.gulimall.coupon.dao;

import com.ty.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 22:47:56
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
