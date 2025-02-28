package cn.rhzhz.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeQuery {
    private Integer userId;
    private String cuisineType;
    // 可扩展其他查询条件
}
