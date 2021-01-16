package com.ty.gulimall.product.web;

import com.ty.gulimall.product.entity.CategoryEntity;
import com.ty.gulimall.product.service.CategoryService;
import com.ty.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping({"/", "index", "/index.html"})
    public String indexPage(Model model) {
        // 获取一级分类所有缓存
        List<CategoryEntity> categorys = categoryService.getLevel1Categorys();
        model.addAttribute("catagories", categorys);
        return "index";
    }

    @ResponseBody
    @RequestMapping("index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatlogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }

    /**
     * 闭锁 只有设定的人全通过才关门
     */
    @ResponseBody
    @GetMapping("/index/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 设置这里有5个人
        door.trySetCount(5);
        door.await();

        return "5个人全部通过了...";
    }

    @ResponseBody
    @GetMapping("/index/go/{id}")
    public String go(@PathVariable("id") Long id) throws InterruptedException {

        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        // 每访问一次相当于出去一个人
        door.countDown();
        return id + "走了";
    }

    /**
     * 尝试获取车位 [信号量]
     * 信号量:也可以用作限流
     */
    @ResponseBody
    @GetMapping("/index/park")
    public String park() {

        RSemaphore park = redissonClient.getSemaphore("park");
        boolean acquire = park.tryAcquire();
        return "获取车位 =>" + acquire;
    }

    /**
     * 尝试获取车位
     */
    @ResponseBody
    @GetMapping("/index/go/park")
    public String goPark() {

        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();
        return "ok => 车位+1";
    }

    /**
     * 读写锁
     */
    @GetMapping("/index/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/index/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        String s = "";
        rLock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }
}
