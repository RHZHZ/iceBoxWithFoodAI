package cn.rhzhz.mapper;

import cn.rhzhz.enumType.FavoriteEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    @Insert("INSERT INTO recipe_favorites (user_id, recipe_id) VALUES (#{userId}, #{recipeId})")
    int insertFavorite(@Param("userId") Integer userId, @Param("recipeId") Long recipeId);

    @Delete("DELETE FROM recipe_favorites WHERE user_id = #{userId} AND recipe_id = #{recipeId}")
    int deleteFavorite(@Param("userId") Integer userId, @Param("recipeId") Long recipeId);

    @Select("SELECT * FROM recipe_favorites WHERE user_id = #{userId} ORDER BY favorite_time DESC")
    List<FavoriteEntity> selectFavoritesByUser(@Param("userId") Integer userId);
}
