package cn.rhzhz.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientVO {
    private String name;     // 食材名称
    private String quantity; // 格式示例："2个"
}
