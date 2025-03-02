package cn.rhzhz.controller;

import cn.rhzhz.DTO.Result;
import cn.rhzhz.pojo.FoodsCategory;
import cn.rhzhz.service.FoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/foodsCategory")
public class FoodsCategoryController {

    @Autowired
    private FoodsCategoryService foodsCategoryService;

    //新增食物分类
    @PostMapping("/add")
    public Result add(@RequestBody @Validated FoodsCategory foodsCategory){
        foodsCategoryService.add(foodsCategory);
        return Result.success();
    }

    //根据名称搜寻分类
    @PatchMapping("/searchCategoryIdByName")
    public Result<FoodsCategory> searchCategoryIdByName(@RequestParam String name){
        FoodsCategory foodsCategory = foodsCategoryService.searchCategoryIdByName(name);

        return Result.success(foodsCategory);
    }
}
