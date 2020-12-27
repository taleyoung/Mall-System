# Mall-System

基于SpringCloud分布式的电商系统，包含商品服务，优惠券，购物车，会员系统，订单系统，仓储服务等。

## 技术选型

### 概览(暂定)

SpringBoot(2.1.8.RELEASE) 

SpringCloud(Greenwich.SR3) 

SpringCloud Alibaba(2.1.0.RELEASE)

mysql(5.7)

（版本后续升级

### 微服务组件选型

SpringCloud Alibaba - Nacos ： 注册中心（服务发现/注册）

SpringCloud Alibaba - Nacos ：配置中心（动态配置管理）

SpringCloud Ribbon : 负载均衡

SpringCloud Feign：声明式HTTP客户端（调用远程服务）

SpringCloud Alibaba - Sentinel：服务容错（限流，降级，熔断）

SpringCloud Gateway：API网关（webflux编程模式）

SpringCloud Sleuth：调用链监控

SpringCloud Alibaba - Seata：原Fescar 分布式事务解决方案

## 服务概览

### 各服务端口

nacos服务发现中心：port 8848

admin 后台管理系统：port 8002


gateway 网关服务：port 88

coupon 优惠券服务：port 7000

member 会员服务：port 8000

order 订单服务：port 9000

product 商品服务：port 10000

ware 仓储服务：port 11000

## 实现细节







