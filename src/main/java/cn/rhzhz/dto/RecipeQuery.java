package cn.rhzhz.DTO;

import cn.rhzhz.pojo.RecipeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeQuery extends RecipeEntity {
    private Integer userId;
    private String cuisineType;
    // 可扩展其他查询条件
}
