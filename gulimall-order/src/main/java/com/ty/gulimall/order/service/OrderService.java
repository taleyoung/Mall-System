package com.ty.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ty.common.utils.PageUtils;
import com.ty.gulimall.order.entity.OrderEntity;
import com.ty.gulimall.order.vo.OrderConfirmVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author taleyoung
 * @email tengye1314@foxmail.com
 * @date 2020-12-23 23:06:52
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;
}

