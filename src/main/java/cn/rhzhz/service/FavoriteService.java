package cn.rhzhz.service;

import cn.rhzhz.DTO.IngredientVO;
import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.controller.RecipeController;
import cn.rhzhz.enumType.FavoriteEntity;
import cn.rhzhz.mapper.FavoriteMapper;
import cn.rhzhz.mapper.RecipeMapper;
import cn.rhzhz.pojo.PageResult;
import cn.rhzhz.pojo.RecipeEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final RecipeMapper recipeMapper;
    @Autowired
    private RecipeController recipeController;

    // 添加收藏
    @Transactional
    public Result<?> addFavorite(Integer userId, Long recipeId) {
        if (favoriteMapper.insertFavorite(userId, recipeId) > 0) {
            return Result.success("收藏成功");
        }
        return Result.error("收藏失败");
    }

    // 取消收藏
    @Transactional
    public Result<?> removeFavorite(Integer userId, Long recipeId) {
        if (favoriteMapper.deleteFavorite(userId, recipeId) > 0) {
            return Result.success("取消收藏成功");
        }
        return Result.error("取消收藏失败");
    }

    // 获取用户收藏列表（带分页）
    public PageResult<RecipeResponse> listFavorites(Integer userId, int page, int size) {
        // 1. 查询收藏关系
        PageHelper.startPage(page, size);
        List<FavoriteEntity> favorites = favoriteMapper.selectFavoritesByUser(userId);
        PageInfo<FavoriteEntity> pageInfo = new PageInfo<>(favorites);

        // 2. 批量获取菜谱详情（避免N+1查询）
        List<Long> recipeIds = favorites.stream()
                .map(FavoriteEntity::getRecipeId)
                .collect(Collectors.toList());

        List<RecipeEntity> recipes = recipeMapper.selectByIds(recipeIds); // 需实现

        // 3. 数据转换
        Map<Long, RecipeEntity> recipeMap = recipes.stream()
                .collect(Collectors.toMap(RecipeEntity::getId, r -> r));

        List<RecipeResponse> data = favorites.stream()
                .map(fav -> recipeMap.get(fav.getRecipeId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 4. 返回正确分页总数
        PageResult pageResult = new PageResult<>();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setData(data);
        return pageResult;
    }

    private RecipeResponse convertToResponse(RecipeEntity entity) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<IngredientVO> ingredients = mapper.readValue(
                    entity.getIngredientsJson(),
                    new TypeReference<List<IngredientVO>>() {}
            );
            List<String> steps = mapper.readValue(
                    entity.getStepsJson(),
                    new TypeReference<List<String>>() {}
            );

            return new RecipeResponse(
                    ingredients,
                    steps,
                    entity.getCuisineType(),
                    entity.getCookingTime(),
                    entity.getCalories(),
                    entity.getHealthAdvice()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("菜谱数据解析失败", e);
        }
    }
}