package cn.rhzhz.service;

import cn.rhzhz.aiService.ModelServiceFactory;
import cn.rhzhz.DTO.AIFoodItem;
import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodRecord;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.Null;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FoodInventoryService {

    @Autowired
    private final ModelServiceFactory modelServiceFactory; // 确保已注入

    @Autowired
    private final FoodsMapper foodRecordMapper;

    @Transactional
    public Flux<String> processConsumption(String message, String provider, int id) throws JSONException {
        // 匹配多食品的正则（支持"g/克/块/个"单位）
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+?):(\\d+)(g|克|块|个)");

        return modelServiceFactory.getService(provider)
                .streamChatCompletion(message)
                .collectList() // 收集完整响应
                .flatMapMany(responseList -> {
                    String fullResponse = String.join("", responseList);
                    // 按分号分割多食品条目
                    String[] foodItems = fullResponse.split(";");

                    return Flux.fromArray(foodItems)
                            .flatMap(item -> {
                                Matcher matcher = pattern.matcher(item);
                                if (matcher.find()) {
                                    AIFoodItem food = new AIFoodItem();
                                    food.setUserId(id);
                                    food.setFoodName(matcher.group(1));
                                    food.setAmount(new BigDecimal(matcher.group(2)));
                                    food.setUnit(matcher.group(3));

                                    try {
                                        food.validate();
                                        // 查询数据库（返回List处理重复）
                                        List<FoodRecord> records = foodRecordMapper.findByName(food.getFoodName(), id);
                                        if (records.isEmpty()) {
                                            return Flux.just(food.getFoodName() + ": 食品不存在");
                                        } else if (records.size() > 1) {
                                            return Flux.just(food.getFoodName() + ": 存在重复食品记录");
                                        }

                                        // 更新库存逻辑
                                        FoodRecord record = records.get(0); // 安全获取唯一记录

                                        if(record.getTotalQuantity()== null){
                                            record.setRemainingQuantity(record.getTotalQuantity().subtract(food.getAmount()));
                                        }
                                        else {
                                            record.setRemainingQuantity(record.getRemainingQuantity().subtract(food.getAmount()));
                                        }

                                        //再次校验
                                        if (record.getRemainingQuantity().compareTo(BigDecimal.ZERO) < 0) {
                                            // 是负数
                                            return Flux.just("库存不足："+food.getFoodName() );
                                        }

                                        foodRecordMapper.update(record);

                                        return Flux.just(food.getFoodName() + " 更新成功，剩余 " + record.getRemainingQuantity() + record.getUnitType());
                                    } catch (Exception e) {
                                        return Flux.just(food.getFoodName() + " 处理失败: " + e.getMessage());
                                    }
                                } else {
                                    return Flux.just("未知格式: " + item);
                                }
                            });
                });
    }
}