package cn.rhzhz.service.impl;

import cn.rhzhz.DTO.IngredientVO;
import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.controller.RecipeController;
import cn.rhzhz.enumType.FavoriteEntity;
import cn.rhzhz.mapper.FavoriteMapper;
import cn.rhzhz.mapper.RecipeMapper;
import cn.rhzhz.pojo.PageBean;
import cn.rhzhz.pojo.RecipeEntity;
import cn.rhzhz.service.FavoriteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
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
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final RecipeMapper recipeMapper;
    @Autowired
    private RecipeController recipeController;

    // 添加收藏
    @Transactional
    public void addFavorite(Integer userId, Long recipeId) {
       favoriteMapper.insertFavorite(userId, recipeId);
    }

    // 取消收藏
    @Transactional
    public void removeFavorite(Integer userId, Long recipeId) {
        favoriteMapper.deleteFavorite(userId, recipeId);

    }

    // 获取用户收藏列表（带分页）
    public PageBean<RecipeResponse> listFavorites(Integer userId, int page, int size) {

        //1.创建PageBean对象
        PageBean<RecipeResponse> pageResult = new PageBean<>();
        //2.开启分页查询
        PageHelper.startPage(page,size);
        //3.调用Mapper
        List<FavoriteEntity> favorites = favoriteMapper.selectFavoritesByUser(userId);
        PageInfo<FavoriteEntity> pageInfo = new PageInfo<>(favorites);
        // 3.1 查询收藏关系
        // 3.2 批量获取菜谱详情（避免N+1查询）
        List<Long> recipeIds = favorites.stream()
                .map(FavoriteEntity::getRecipeId)
                .collect(Collectors.toList());

        List<RecipeEntity> recipes = recipeMapper.selectByIds(recipeIds);

        // 3.3 数据转换
        Map<Long, RecipeEntity> recipeMap = recipes.stream()
                .collect(Collectors.toMap(RecipeEntity::getId, r -> r));

        List<RecipeResponse> data = favorites.stream()
                .map(fav -> recipeMap.get(fav.getRecipeId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 3.4 返回正确分页总数
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setItems(data);

        return pageResult;

    }

    @Override
    public PageBean<RecipeResponse> listFavoritesByType(Integer userId, int page, int size, String cuisineType) {
        //1.创建PageBean对象
        PageBean<RecipeResponse> pageResult = new PageBean<>();
        //2.开启分页查询
        PageHelper.startPage(page,size);
        //3.调用Mapper
        List<FavoriteEntity> favorites = favoriteMapper.listFavoritesByType(userId,page,size,cuisineType);

        //4.Page中提供方法，可以获取PageHelper分页查询后得到的总记录条数和当前页数据，所以进行强转
        ////直接使用 PageInfo 封装结果：避免强制转换，改用 PageInfo 对象接收分页数据
        PageInfo<FavoriteEntity> pageInfo = new PageInfo<>(favorites);
//        Page<RecipeResponse> p = (Page<RecipeResponse>) recipeResponseList;
        // 4.1 查询收藏关系
        // 4.2 批量获取菜谱详情（避免N+1查询）
        List<Long> recipeIds = favorites.stream()
                .map(FavoriteEntity::getRecipeId)
                .collect(Collectors.toList());

        List<RecipeEntity> recipes = recipeMapper.selectByIds(recipeIds);

        // 4.3 数据转换
        Map<Long, RecipeEntity> recipeMap = recipes.stream()
                .collect(Collectors.toMap(RecipeEntity::getId, r -> r));

        List<RecipeResponse> data = favorites.stream()
                .map(fav -> recipeMap.get(fav.getRecipeId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        //5.把数据填充到PageBean对象中
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setItems(data);
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
