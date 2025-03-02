package cn.rhzhz.service;

import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.pojo.FoodTypeCount;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FoodsRecordService {

    //获取所有食物信息
    List<FoodRecord> getFoodList();
    //获取所有食物信息
    List<FoodRecord> getEatFoodList();
    //获取所有食物信息
    List<FoodRecord> getNormalFoodList();
    //获取所有食物信息
    List<FoodRecord> getThrowFoodList();
    //获取用户的所有分类
    List<FoodTypeCount> groupByType();

    //增加一条库存记录
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 每条记录独立事务
    void addRecord(FoodRecord foodRecord);

    //删除一条记录
    void delRecord(int foodRecordId);

    //获取浪费的总价
    BigDecimal getTotalPriceOfThrownFood();


    //根据分类获取食物记录
    List<FoodRecord> getFoodRecordBytype(String type);

    //获取临期产品
    List<FoodRecord> getExpiringFoodRecords();

    //模糊查询
    List<FoodRecord> searchByName(String name);

    //消耗食物
    FoodRecord consumeFood(int foodId, BigDecimal amount);

    //根据ID查食物记录
    FoodRecord findById(int foodId);

    //修改食物记录
    void updateRecord(FoodRecord foodRecord);

    //如果存在同种食品名，通过判断添加日期来判断是否重复添加
    boolean existsByUserAndNameAndDate(Integer userId, String name, LocalDate purchaseDate);

    //更新记录
    void update(FoodRecord record);

    //通过名字查找记录
    List<FoodRecord> findByName(String foodName, int id);
}
