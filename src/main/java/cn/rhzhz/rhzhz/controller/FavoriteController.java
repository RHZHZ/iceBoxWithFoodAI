package cn.rhzhz.controller;

import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.mapper.FavoriteMapper;
import cn.rhzhz.pojo.PageBean;
import cn.rhzhz.pojo.PageResult;
import cn.rhzhz.pojo.RecipeEntity;
import cn.rhzhz.service.FavoriteService;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    //add favorite recipe
    @PostMapping("/add")
    public Result addFavorite(@RequestParam Long recipeId) {
        //待增：菜谱存在性
        Integer userId = getCurrentUserId(); // 从会话获取用户ID
        favoriteService.addFavorite(userId, recipeId);
        return Result.success();
    }

    //remove favorite recipe
    @DeleteMapping("/remove")
    public Result removeFavorite(@RequestParam Long recipeId) {
        //待增：菜谱存在性
        Integer userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, recipeId);
        return Result.success();
    }

    //展示收藏的菜谱-cuisineType菜谱类型(可选)
    @GetMapping("/list")
    public Result<PageBean<RecipeResponse>>listFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Integer userId = getCurrentUserId();
        return Result.success(favoriteService.listFavorites(userId, page, size));
    }

    //根据类型展示收藏的菜谱-cuisineType菜谱(可选)，默认所有
    @GetMapping("/listFavoritesByType")
    public Result<PageBean<RecipeResponse>>listFavoritesByType(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cuisineType
    ) {
        Integer userId = getCurrentUserId();
        PageBean<RecipeResponse> pageBean = favoriteService.listFavoritesByType(userId, page, size, cuisineType);
        return Result.success(pageBean);
    }
//    @GetMapping("/check")
//    public Result<Boolean> checkFavorite(@RequestParam Long recipeId) {
//        Integer userId = getCurrentUserId();
//        int count = favoriteMapper.countFavorite(userId, recipeId);
//        return Result.success(count > 0);
//    }

    // 获取当前用户ID（示例）
    private Integer getCurrentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        int userId = (int) map.get("id");
        return userId;
    }
}
