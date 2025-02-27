package cn.rhzhz.service.impl;

import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.service.FoodsRecordService;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class FoodsRecordRecordServiceImpl implements FoodsRecordService {
    @Autowired
    private FoodsMapper foodsMapper;
    @Override
    public List<FoodRecord> getFoodList() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.getFoodList(id);
    }

    @Override
    public List<FoodRecord> getEatFoodList() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.getEatFoodList(id);
    }

    @Override
    public List<FoodRecord> getNormalFoodList() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.getNormalFoodList(id);
    }

    @Override
    public List<FoodRecord> getThrowFoodList() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.getThrowFoodList(id);
    }

    @Override
    public void addRecord(FoodRecord foodRecord) {

        foodsMapper.addRecord(foodRecord);
    }

    @Override
    public void delRecord(int foodRecordId) {
        foodsMapper.delRecord(foodRecordId);
    }

    @Override
    public BigDecimal getTotalPriceOfThrownFood() {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.getTotalPriceOfThrownFood(id);
    }


    @Override
    public List<FoodRecord> getExpiringFoodRecords() {
        LocalDate startDate = LocalDate.now(); // 当前日期
        LocalDate endDate = startDate.plusDays(7); // 未来 7 天的日期
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        System.out.println(startDate+"-"+endDate);
        System.out.println("id"+id);
        return foodsMapper.getExpiringFoodRecords(startDate, endDate,id);
    }

    @Override
    public List<FoodRecord> searchByName(String name) {
        // 从 ThreadLocal 中获取当前用户 ID
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        return foodsMapper.searchByName(id, name);
    }

    /**
     * 消耗食物并更新数据库
     * @param foodId 食物记录ID
     * @param amount 消耗数量
     * @return 更新后的食物记录
     */
    @Transactional
    public FoodRecord consumeFood(int foodId, BigDecimal amount) {
        // 1. 查询食物记录
        FoodRecord food = foodsMapper.findById(foodId);
        if (food == null) {
            throw new IllegalArgumentException("食物记录不存在");
        }

        // 2. 执行消耗操作
        food.consume(amount);

        // 3. 更新数据库
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        food.setUserId(id);
        try {
            foodsMapper.update(food);
            return food;
        } catch (Exception e) {
            throw e;
        }


    }

    @Override
    public FoodRecord findById(int foodId) {
        return foodsMapper.findById(foodId);
    }

    @Override
    public List<FoodRecord> getFoodRecordBytype(String type) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        System.out.println("id"+id);
        return foodsMapper.getFoodRecordBytype(id,type);
    }
}
