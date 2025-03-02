package cn.rhzhz.pojo;

import cn.rhzhz.DTO.FoodRecordDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;


// 食品基础实体
//一个用户多个食品记录，一条记录属于一个用户，一个食品属于一个夹层
@Data
public class FoodRecord {

    private int id;
    @NotBlank
    private int userId;
    @NotBlank
    private String name; // 标准名称（如"全麦面包"）
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate; // 购买日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate; // 保质期截止日
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate created_time;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate update_time;
    private BigDecimal totalQuantity; // 初始总量
    private BigDecimal remainingQuantity; // 当前剩余量
    private BigDecimal price; // 购买价格
    private int info; // 食物状态0正常1吃完2浪费
    private String unitType; // 单位类型（克/毫升/片）
    private String type; // 食品分类（如"主食","乳制品"）
    private String imgUrl; // 食品图
    //private String batchTag; // 批次标识（如"20240220-001"）


    //private JsonNode nickName; // 别名列表（如["黑麦面包","全麦吐司"]）

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

    //Dto实体转换
    public FoodRecord convertToEntity(FoodRecordDTO dto, Integer userId) {
        FoodRecord foodRecord = new FoodRecord();
        foodRecord.setUserId(userId);
        foodRecord.setName(dto.getName());
        foodRecord.setImgUrl(dto.getImgUrl());
        foodRecord.setExpirationDate(dto.getExpirationDate());
        foodRecord.setPurchaseDate(dto.getPurchaseDate());
        foodRecord.setPrice(dto.getPrice());
        foodRecord.setUnitType(dto.getUnitType());
        foodRecord.setType(dto.getType());
        foodRecord.setTotalQuantity(dto.getTotalQuantity());
        foodRecord.setRemainingQuantity(dto.getRemainingQuantity());
        foodRecord.setInfo(dto.getInfo());
        return foodRecord;
    }
}
