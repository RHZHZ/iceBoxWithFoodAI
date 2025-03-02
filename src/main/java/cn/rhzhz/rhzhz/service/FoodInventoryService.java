package cn.rhzhz.service;

import cn.rhzhz.aiService.ModelServiceFactory;
import cn.rhzhz.DTO.AIFoodItem;
import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.Null;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FoodInventoryService {

    @Autowired
    private final ModelServiceFactory modelServiceFactory; // 确保已注入

    @Autowired
    private final FoodsMapper foodRecordMapper;

    @Autowired
    private FoodsRecordService foodsRecordService;

    @Value("${ai.providers.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.providers.deepseek.base-url}")
    private String baseUrl;

    public Flux<String> callAIProvider(String prompt, String provider) {
        // deepseekService：
        WebClient client = WebClient.create(baseUrl);

        return client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "messages", List.of(Map.of("role", "user", "content", prompt)),
                        "stream", true,
                        "model","deepseek-chat"
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::extractContent);
    }

    private String extractContent(String response) {
        try {
            JsonNode node = new ObjectMapper().readTree(response);
            return node.path("choices").get(0).path("delta").path("content").asText();
        } catch (Exception e) {
            return "";
        }
    }

    // 修复库存更新逻辑
    @Transactional
    public Flux<String> processConsumption(String message, String provider, int id) throws JSONException {
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+?):(\\d+)(g|克|块|个)");

        return modelServiceFactory.getService(provider)
                .streamChatCompletion(message)
                .collectList()
                .flatMapMany(responseList -> {
                    String fullResponse = String.join("", responseList);
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
                                        List<FoodRecord> records = foodsRecordService.findByName(food.getFoodName(), id);
                                        if (records.isEmpty()) {
                                            return Flux.just(food.getFoodName() + ": 食品不存在");
                                        }
                                        //多记录只取一条
                                        FoodRecord record = records.get(0);
                                        BigDecimal remaining = record.getRemainingQuantity() != null ?
                                                record.getRemainingQuantity() : record.getTotalQuantity();

                                        if(remaining == null) {
                                            return Flux.just(food.getFoodName() + ": 库存数据异常");
                                        }

                                        BigDecimal newRemaining = remaining.subtract(food.getAmount());
                                        if(newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                                            return Flux.just("库存不足：" + food.getFoodName());
                                        }

                                        record.setRemainingQuantity(newRemaining);
                                        foodsRecordService.update(record);

                                        return Flux.just(food.getFoodName() + " 更新成功，剩余 " + newRemaining + record.getUnitType());
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