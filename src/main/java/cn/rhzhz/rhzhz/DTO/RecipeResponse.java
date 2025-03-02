package cn.rhzhz.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private List<IngredientVO> ingredients; // 食材清单
    private List<String> steps;             // 烹饪步骤
    private String cuisineType;             // 菜系
    private String cookingTime;             // 烹饪时间
    private String calories;               // 热量（如"300大卡"）
    private String healthAdvice;            // 健康建议


}
