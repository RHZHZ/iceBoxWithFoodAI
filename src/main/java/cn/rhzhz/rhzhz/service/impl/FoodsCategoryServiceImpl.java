package cn.rhzhz.service.impl;

import cn.rhzhz.mapper.FoodsCategoryMapper;
import cn.rhzhz.pojo.FoodsCategory;
import cn.rhzhz.service.FoodsCategoryService;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class FoodsCategoryServiceImpl implements FoodsCategoryService {
    @Autowired
    FoodsCategoryMapper foodsCategoryMapper;

    @Override
    public void add(FoodsCategory foodsCategory) {
        //补充属性值
        foodsCategory.setCreateTime(LocalDateTime.now());
        foodsCategory.setUpdateTime(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        foodsCategory.setCreateUser(userId);
        foodsCategoryMapper.add(foodsCategory);
    }

    @Override
    public FoodsCategory searchCategoryIdByName(String name) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        return foodsCategoryMapper.searchCategoryIdByName(name,userId);
    }
}
