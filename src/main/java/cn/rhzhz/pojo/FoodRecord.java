package cn.rhzhz.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;


// 食品基础实体
@Data
public class FoodRecord {
//一个用户多个食品记录，一条记录属于一个用户，一个食品属于一个夹层
    private int id;

    private int userId;

    private String name; // 标准名称（如"全麦面包"）


//    private JsonNode nickName; // 别名列表（如["黑麦面包","全麦吐司"]）

    private LocalDate purchaseDate; // 购买日期
    private LocalDate expirationDate; // 保质期截止日
    private BigDecimal totalQuantity; // 初始总量
    private BigDecimal remainingQuantity; // 当前剩余量

    private BigDecimal price; // 购买价格
//    private String batchTag; // 批次标识（如"20240220-001"）
    private int info; // 食物状态0正常1吃完2浪费


    private String unitType; // 单位类型（克/毫升/片）

    private String type; // 食品分类（如"主食","乳制品"）

    private String imgUrl; // 食品图

    /**
     * 消耗食物
     * @param amount 消耗的数量（必须大于0）
     * @throws IllegalArgumentException 如果消耗量不合法
     */
    public void consume(BigDecimal amount) {
        // 参数校验
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("消耗量必须大于0");
        }

        // 计算剩余量
        BigDecimal newRemaining = remainingQuantity.subtract(amount);

        // 如果剩余量小于0，强制设为0
        if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
            newRemaining = BigDecimal.ZERO;
        }

        // 更新剩余量
        this.remainingQuantity = newRemaining;

        // 更新食物状态：如果剩余量为0，标记为已吃完（状态1）
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            this.info = 1;
        }
    }
}
