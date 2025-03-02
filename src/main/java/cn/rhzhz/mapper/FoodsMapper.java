package cn.rhzhz.mapper;

import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.pojo.FoodTypeCount;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Mapper
public interface FoodsMapper {

    //获取所有食物
    @Select("select * from food_record where user_id = #{id}")
    List<FoodRecord> getFoodList(Integer id);

    //获取正常状态食物0
    @Select("select * from food_record where user_id = #{id} and info = '0'")
    List<FoodRecord> getNormalFoodList(Integer id);

    //获取吃完了的食物1
    @Select("select * from food_record where user_id = #{id} and info = '1'")
    List<FoodRecord> getEatFoodList(Integer id);

    //获取丢掉了的食物2
    @Select("select * from food_record where user_id = #{id} and info = '2'")
    List<FoodRecord> getThrowFoodList(Integer id);

    //获取浪费的总价
    @Select("SELECT SUM(price) AS total_price FROM food_record WHERE info = '2' and user_id = #{id}")
    BigDecimal getTotalPriceOfThrownFood(Integer id);

    //获取对应分类下的食物
    @Select("select * from food_record where user_id = #{id} and info = '0' and type = #{type}")
    List<FoodRecord> getFoodRecordBytype(Integer id,String type);

    //获取临期产品
    @Select("SELECT * FROM food_record WHERE expiration_date BETWEEN #{startDate} AND #{endDate} AND user_id = #{userId} AND info = '0'")
    List<FoodRecord> getExpiringFoodRecords(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("userId") Integer userId);

    //获取冰箱食物的总价
//    @Select("SELECT SUM(price) AS total_price FROM food_record WHERE info = '2' and user_id = #{id}")
//    BigDecimal getTotalPriceOfThrownFood(Integer id);

    //增加一条库存记录
    @Insert({
            "INSERT INTO food_record (user_id,type, name,price,info,unit_type,created_time, purchase_date, expiration_date, " +
                    "total_quantity, remaining_quantity,img_url)",
            "VALUES (#{userId}, #{type},#{name},#{price}, #{info},#{unitType}," +
                    " NOW(), #{purchaseDate}, #{expirationDate}, #{totalQuantity}, #{remainingQuantity},#{imgUrl})"
    })
    void addRecord(FoodRecord foodRecord);

    /**
     * 根据食物名称模糊查询（匹配当前用户）
     * @param userId 用户 ID
     * @param name 食物名称关键词
     * @return 匹配的食品记录列表
     */
    @Select("SELECT * FROM food_record " +
            "WHERE user_id = #{userId} AND name LIKE CONCAT('%', #{name}, '%')")
    List<FoodRecord> searchByName(@Param("userId") Integer userId, @Param("name") String name);

    //删除一条记录
    void delRecord(int foodRecordId);


    //查询食物Byid
    @Select("select * from food_record where id = #{foodId}")
    FoodRecord findById(int foodId);

    // 更新余量记录
    @Update("UPDATE food_record SET " +
            "remaining_quantity = #{remainingQuantity}, " +
            "info = #{info} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    void update(FoodRecord food);

    // 返回可能包含多个记录的 List
    @Select("SELECT * FROM food_record WHERE name = #{name} AND user_id = #{userId}")
    List<FoodRecord> findByName(@Param("name") String name, @Param("userId") Integer userId);

    //根据分类统计记录数量并返回
    @Select("SELECT type, COUNT(*) AS count FROM food_record WHERE user_id = 7 GROUP BY type")
    List<FoodTypeCount> groupByType(@Param("userId") int userId);

    @Select("SELECT COUNT(*) FROM food_record " +
            "WHERE user_id = #{userId} " +
            "AND name = #{name} " +
            "AND purchase_date = #{purchaseDate}")
    boolean existsByUserAndNameAndDate(Integer userId, String name, LocalDate purchaseDate);

    //修改一条库存记录
    @Update({
            "UPDATE food_record " +
            "SET" +
            "    type = #{type}, " +
            "    name = #{name}, " +
            "    price = #{price}," +
            "    info = #{info}," +
            "    unit_type = #{unitType}," +
            "    update_time = NOW()," +
            "    purchase_date = #{purchaseDate}," +
            "    expiration_date = #{expirationDate}," +
            "    total_quantity = #{totalQuantity}," +
            "    remaining_quantity = #{remainingQuantity}," +
            "    img_url = #{imgUrl}" +
            "WHERE id = #{id} AND user_id = #{userId}"
    })
    void updateRecord(FoodRecord foodRecord);
}
