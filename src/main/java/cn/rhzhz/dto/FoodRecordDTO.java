package cn.rhzhz.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class FoodRecordDTO {
    @NotBlank(message = "食品名称不能为空")
    private String name; // 标准名称（如"全麦面包"）

//    private JsonNode nickName; // 别名列表（如["黑麦面包","全麦吐司"]）

    @NotNull(message = "购买日期不能为空")
    private LocalDate purchaseDate; // 购买日期

    @NotNull(message = "保质期截止日不能为空")
    private LocalDate expirationDate; // 保质期截止日

    @DecimalMin(value = "0.0", inclusive = false, message = "初始总量必须大于0")
    private BigDecimal totalQuantity; // 初始总量

    @DecimalMin(value = "0.0", message = "当前剩余量不能为负数")
    private BigDecimal remainingQuantity; // 当前剩余量

    @NotNull(message = "购买价格不能为空")
    @DecimalMin(value = "0.0", message = "购买价格不能为负数")
    private BigDecimal price; // 购买价格

    private String batchTag; // 批次标识（如"20240220-001"）

    @Range(min = 0, max = 2, message = "食物状态异常")
    private int info; // info:食物状态

    @NotNull(message = "单位类型不能为空")
    private String unitType; // 单位类型（克/毫升/片）

    @NotNull(message = "食品分类不能为空")
    private String type; // 食品分类（如"主食","乳制品"）

    @URL(message = "图片URL格式不正确")
    private String imgUrl; // 食品图片

    // Getter 和 Setter
    // ...
}