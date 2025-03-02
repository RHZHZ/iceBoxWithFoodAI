package cn.rhzhz.utils;

import cn.rhzhz.DTO.FoodRecordDTO;
import cn.rhzhz.mapper.FavoriteMapper;
import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.service.FoodsRecordService;
import jakarta.validation.ValidationException;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component
@NoArgsConstructor
public class FoodRecordTools {

    @Autowired
    FoodsRecordService foodsRecordService;
    @Autowired
    FoodsMapper foodsMapper;

    private static final Logger logger = LoggerFactory.getLogger(FoodRecordTools.class);

    public BigDecimal parseQuantity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return BigDecimal.ZERO; // 或抛 IllegalArgumentException
        }

        // 保留数字、负号、小数点，替换逗号为点
        String numStr = input.replaceAll("[^0-9.-]", "")
                .replace(",", ".");

        // 处理多小数点或无效格式
        if (numStr.isEmpty() || numStr.matches(".*\\..*\\..*")) {
            return BigDecimal.ZERO;
        }

        // 清理符号和格式
        numStr = numStr.replaceAll("(?<!^)-", ""); // 只允许开头的负号
        numStr = numStr.replaceAll("^(-?)0+(\\d+)", "$1$2"); // 移除前导零

        try {
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO; // 或抛自定义异常
        }
    }

    // 独立校验方法
    public List<String> validateDTO(FoodRecordDTO dto) {
        List<String> errors = new ArrayList<>();
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.add("名称不能为空");
        }
        if (dto.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("总量必须大于0");
        }
        if (dto.getExpirationDate().isBefore(dto.getPurchaseDate())) {
            errors.add("保质期不能早于购买日期");
        }
        return errors;
    }

    // 解析逻辑-支持多条目处理
    public List<FoodRecordDTO> parseAIResponse(String response, Integer userId) throws Exception {

        List<FoodRecordDTO> records = new ArrayList<>();

        //单位转换工具
        FoodRecordTools foodRecordTools = new FoodRecordTools();

        // 按条目分割（支持数字编号或项目符号）
        String[] items = response.split("\\d+\\. |\\* ");

        // 实现解析逻辑
        for (String item : items) {
            if (item.trim().isEmpty()) continue;

            Map<String, String> data = new HashMap<>();
            Pattern pairPattern = Pattern.compile("(名称|类型|购买日期|保质期|总量|价格|单位类型|图片)[:：]\\s*(.+)");

            Matcher matcher = pairPattern.matcher(item);
            while (matcher.find()) {
                data.put(matcher.group(1).trim(), matcher.group(2).trim());
            }
            if (data.containsKey("名称")) {
                FoodRecordDTO dto = new FoodRecordDTO();

                try {
                    dto.setUserId(userId);
                    dto.setName(data.getOrDefault("名称", ""));
                    dto.setTotalQuantity(foodRecordTools.parseQuantity(data.get("总量")));
                    dto.setRemainingQuantity(dto.getTotalQuantity()); // 剩余量等于总量
                    dto.setUnitType(data.get("单位类型"));
                    dto.setType(data.get("类型"));
                    dto.setPrice(foodRecordTools.parseQuantity(data.get("价格")));

                    //根据文字获取一张图片
                    ImageApiClient imageApiClient = new ImageApiClient();
                    String pic = imageApiClient.getImageUrls(data.getOrDefault("名称", "AI"));
                    dto.setImgUrl(pic);
                    logger.info("获取到图片：｛｝",pic);

                    dto.setInfo(0); // 默认正常状态
                } catch (Exception e) {
                    logger.error("parseAIResponseError：{}",e);
                    throw new RuntimeException(e);
                }

                // 日期处理
                try {
                    dto.setExpirationDate(LocalDate.parse(data.get("保质期")));
                } catch (Exception e) {
                    throw new IllegalArgumentException("保质期格式错误");
                }

                if (data.containsKey("购买日期")) {
                    try {
                        dto.setPurchaseDate(LocalDate.parse(data.get("购买日期")));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("购买日期格式错误");
                    }
                } else {
                    dto.setPurchaseDate(LocalDate.now());
                }
                //add a record
                records.add(dto);
            }
        }
        return records;
    }


}
