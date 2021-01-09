package com.ty.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.ty.gulimall.coupon.dao")
public class GulimallCouponApplication {
    public static void main(String[] args){
        SpringApplication.run(GulimallCouponApplication.class, args);
    }
}
