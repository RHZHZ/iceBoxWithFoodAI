package cn.rhzhz.service;

import cn.rhzhz.pojo.FoodRecord;

import java.math.BigDecimal;
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


    //增加一条库存记录
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
}
