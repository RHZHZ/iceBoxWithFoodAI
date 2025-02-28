package cn.rhzhz.mapper;

import cn.rhzhz.DTO.RecipeQuery;
import cn.rhzhz.pojo.RecipeEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecipeMapper {
    @Insert("INSERT INTO recipes (user_id, cuisine_type, cooking_time,create_time, calories, ingredients_json, steps_json, health_advice) " +
            "VALUES (#{userId}, #{cuisineType}, #{cookingTime}, #{createTime},#{calories}, #{ingredientsJson}, #{stepsJson}, #{healthAdvice})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecipe(RecipeEntity entity);

    // 动态查询方法
    @Select("<script>" +
            "SELECT * FROM recipes" +
            "<where>" +
            "    <if test=\"query.userId != null\">" +
            "        user_id = #{query.userId}" +
            "    </if>" +
            "    <if test=\"query.cuisineType != null and query.cuisineType != ''\">" +
            "        AND cuisine_type = #{query.cuisineType}" +
            "    </if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<RecipeEntity> selectByQuery(@Param("query") RecipeQuery query);

    // 新增方法：根据ID查询菜谱
    @Select("SELECT * FROM recipes WHERE id = #{id}")
    RecipeEntity selectById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM recipes WHERE id IN",
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<RecipeEntity> selectByIds(@Param("ids") List<Long> ids);
}
