package cn.rhzhz.mapper;

import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.pojo.FoodsCategory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FoodsCategoryMapper {

    //add a record
    @Insert("INSERT into foods_category(category_name,category_alias,create_user,create_time,update_time)" +
            "VALUES (#{categoryName},#{categoryAlias},#{createUser},#{createTime},#{updateTime})")
    void add(FoodsCategory foodsCategory);

    //根据名称查ID
    @Select("SELECT * FROM foods_category " +
            "WHERE create_user = #{userId} AND category_name = #{name}")
    FoodsCategory searchCategoryIdByName(String name,int userId);
}
