package com.ty.gulimall.order.dao;

import com.ty.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 23:06:52
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
