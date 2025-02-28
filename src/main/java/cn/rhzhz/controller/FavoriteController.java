package cn.rhzhz.controller;

import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.mapper.FavoriteMapper;
import cn.rhzhz.pojo.PageResult;
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
    @Autowired
    private FavoriteMapper favoriteMapper;

    //add favorite recipe
    @PostMapping("/add")
    public Result<?> addFavorite(@RequestParam Long recipeId) {
        //待增：菜谱存在性
        Integer userId = getCurrentUserId(); // 从会话获取用户ID
        return favoriteService.addFavorite(userId, recipeId);
    }

    //remove favorite recipe
    @DeleteMapping("/remove")
    public Result<?> removeFavorite(@RequestParam Long recipeId) {
        //待增：菜谱存在性
        Integer userId = getCurrentUserId();
        return favoriteService.removeFavorite(userId, recipeId);
    }

    //展示收藏的菜谱
    @GetMapping("/list")
    public PageResult<RecipeResponse> listFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer userId = getCurrentUserId();
        return favoriteService.listFavorites(userId, page, size);
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
