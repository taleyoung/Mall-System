package com.ty.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ty.gulimall.product.entity.CategoryBrandRelationEntity;
import com.ty.gulimall.product.service.CategoryBrandRelationService;
import com.ty.gulimall.product.vo.Catalog3Vo;
import com.ty.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ty.common.utils.PageUtils;
import com.ty.common.utils.Query;

import com.ty.gulimall.product.dao.CategoryDao;
import com.ty.gulimall.product.entity.CategoryEntity;
import com.ty.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sun.jvm.hotspot.debugger.Page;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    public List<CategoryEntity> listWithTree(){
        List<CategoryEntity> list = baseMapper.selectList(null);
        List<CategoryEntity> level1 = list.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid()==0)
                .peek(entity -> entity.setChildren(getChildren(entity, list)))
                .sorted((menu1, menu2)->{
                    Integer menu1Sort = menu1.getSort() == null ? 0 : menu1.getSort();
                    Integer menu2Sort = menu2.getSort() == null ? 0 : menu2.getSort();
                    return menu1Sort - menu2Sort;
                })
                .collect(Collectors.toList());

        return level1;
    }

    public void removeMenuByIds(List<Long> asList){
        //TODO: 检查当前删除的菜单，是否被别的地方引用。
        baseMapper.deleteBatchIds(asList);
    }

    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .peek(entity-> entity.setChildren(getChildren(entity, all)))
                .sorted((menu1, menu2)->{
                    Integer menu1Sort = menu1.getSort() == null ? 0 : menu1.getSort();
                    Integer menu2Sort = menu2.getSort() == null ? 0 : menu2.getSort();
                    return menu1Sort - menu2Sort;
                })
                .collect(Collectors.toList());
        return children;
    }

    //拿到三级分类的完整路径
    public Long[] findCatelogPath(Long categoryId){
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(categoryId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联数据
     */
    @Transactional
    public void updateCascade(CategoryEntity categoryEntity){
        this.updateById(categoryEntity);
        categoryBrandRelationService.updateCategory(categoryEntity.getCatId(), categoryEntity.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    /**
     * 三级分类 v1.0
     * 最原始的获取三级分类，无优化。
     * 从数据库中获取三级分类。
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    /**
     * 三级分类 v2.0
     * 三级分类第一版优化：加入redis
     * 从redis中去catelogJson
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonWithRedis(){
        //1.加入缓存逻辑， 缓存中存的都是json
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)){
            //缓存中没有，需要查数据库
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getDataFromDB();
            //查到的数据放入缓存，将对象转为json放入缓存中
            String s = JSON.toJSONString(catelogJsonFromDB);
            redisTemplate.opsForValue().set("catalogJSON", s);
            return catelogJsonFromDB;
        }
        //转为指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    /**
     * 查询数据库的优化
     * 1. 避免在循环中查询数据库
     * 2. 加入redis后进行的一些优化。
     */
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catelogJSON)) {
            return JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        // 优化：将查询变为一次
        List<CategoryEntity> entityList = baseMapper.selectList(null);

        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        // 优化：查询到数据库就再锁还没结束之前放入缓存
        redisTemplate.opsForValue().set("catelogJSON", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * 三级分类 v3.0
     * 为redis加锁。本地锁
     * @return
     */

    public Map<String, List<Catelog2Vo>> getCatelogJsonWithLocalLock(){
        synchronized (this){
            return getDataFromDB();
        }
    }

    /**
     * 三级分类 v4.0
     * redis 加锁， 分布式锁。
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock(){
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 20, TimeUnit.SECONDS);
        if(lock){
            Map<String, List<Catelog2Vo>> data = null;
            try{
                data = getDataFromDB();
            }finally {
                // 删除也必须是原子操作 Lua脚本操作 删除成功返回1 否则返回0
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 原子删锁
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        }else{
            //加锁失败，说明已经有这个锁了，需要重试加锁
            try {
                // 登上两百毫秒
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBWithRedisLock();
        }
    }

    /**
     * 三级分类 v5.0
     * 通过redisson加分布式锁
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedissonLock(){
        // 这里只要锁的名字一样那锁就是一样的
        // 关于锁的粒度 具体缓存的是某个数据 例如: 11-号商品 product-11-lock
        RLock lock = redissonClient.getLock("catelogJson");
        lock.lock();
        Map<String, List<Catelog2Vo>> data;
        try{
            data = getDataFromDB();
        }finally {
            lock.unlock();
        }
        return data;
    }

    /**
     * 第一次查询的所有 CategoryEntity 然后根据 parent_cid去这里找
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parent_cid) {

        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
    }

    public List<Long> findParentPath(Long categoryId, List<Long> paths){
        paths.add(categoryId);
        CategoryEntity byId = this.getById(categoryId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }
}