package cn.rhzhz.service;

import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FoodInventoryService {
    @Autowired
    private final ModelServiceFactory modelServiceFactory; // 确保已注入
    @Qualifier("deepSeekService")
    @Autowired
    private ModelService modelService;
    @Autowired
    private final FoodsMapper foodRecordMapper;

    @Transactional
    public Flux<String> processConsumption(String message, String provider,int id) throws JSONException {
        return modelServiceFactory.getService(provider)
                .streamChatCompletion(message)
                .windowTimeout(15, Duration.ofMillis(2000)) // 每5个块或500ms触发一次处理
                .flatMap(flux -> flux.collect(StringBuffer::new, StringBuffer::append))
                .map(buffer -> {
                    // 解析食物消耗信息（示例：使用正则）
                    Matcher matcher = Pattern.compile("(.+?):(\\d+)(克|千克)").matcher(buffer);
                    if (matcher.find()) {
                        System.out.println("找到！"+matcher);
                        String foodName = matcher.group(1);
                        BigDecimal amount = new BigDecimal(matcher.group(2));
                        String unit = matcher.group(3);

                        // 更新库存

                        FoodRecord food = foodRecordMapper.findByName(foodName,id)
                                .orElseThrow(() -> new IllegalArgumentException("食物不存在"));
                        food.setRemainingQuantity(food.getRemainingQuantity().subtract(amount));
                        foodRecordMapper.update(food);

                        return "更新成功: " + foodName + " 剩余 " + food.getRemainingQuantity() + unit;
                    }
                    return buffer+"";
                });
    }
}