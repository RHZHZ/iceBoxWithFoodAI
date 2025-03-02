package cn.rhzhz.pojo;

import lombok.Data;

@Data
public class FoodTypeCount {
    private String type;   // 分类名称（如生鲜、蔬菜）
    private Integer count; // 该分类下的记录数

}