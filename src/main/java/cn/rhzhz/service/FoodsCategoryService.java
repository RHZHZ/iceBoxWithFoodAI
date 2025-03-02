package cn.rhzhz.service;

import cn.rhzhz.pojo.FoodsCategory;

public interface FoodsCategoryService {
    //新增分类
    void add(FoodsCategory foodsCategory);
    //根据名称查ID
    FoodsCategory searchCategoryIdByName(String name);
}
