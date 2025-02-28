package cn.rhzhz.DTO;

import io.micrometer.common.util.StringUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Arrays;

@Data
public class AIFoodItem {
    private String foodName;
    private BigDecimal amount;
    private String unit;
    private Integer userId;

    // 校验方法
    public void validate() {
        if (StringUtils.isBlank(foodName)) {
            throw new IllegalArgumentException("食品名称不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (!Arrays.asList("克", "千克", "个", "块").contains(unit)) {
            throw new IllegalArgumentException("不支持的单位类型: " + unit);
        }
    }
}
