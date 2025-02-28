package cn.rhzhz.DTO;

import lombok.Data;

import java.util.List;

@Data
public class RecipeDTO {
    private List<Ingredient> ingredients;
    private List<String> steps;
    private String cuisineType;
    private int cookingTime;

    @Data
    public static class Ingredient {
        private String name;
        private double quantity;
        private String unit;
    }
}
