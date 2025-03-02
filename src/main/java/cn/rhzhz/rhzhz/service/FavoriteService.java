package cn.rhzhz.service;

import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.pojo.PageBean;


public interface FavoriteService {
   //添加收藏
   void addFavorite(Integer userId, Long recipeId);
   //取消收藏
   void removeFavorite(Integer userId, Long recipeId);
   //获取用户收藏列表（带分页）
   PageBean<RecipeResponse> listFavorites(Integer userId, int page, int size);
   //根据类型展示收藏菜谱
   PageBean<RecipeResponse> listFavoritesByType(Integer userId, int page, int size, String cuisineType);
}