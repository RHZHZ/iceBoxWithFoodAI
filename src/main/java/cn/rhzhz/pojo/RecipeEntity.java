package cn.rhzhz.pojo;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

// RecipeEntity.java
@Data
@Table(name = "recipes")
public class RecipeEntity {

    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(length = 100)
    private String cuisineType;

    @Column(length = 50)
    private String cookingTime;

    @Column(length = 50)
    private String calories;

    @Column(columnDefinition = "TEXT")
    private String ingredientsJson; // 食材列表JSON

    @Column(columnDefinition = "TEXT")
    private String stepsJson;       // 步骤列表JSON

    @Column(length = 20)
    private String healthAdvice;

    @Column(updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
}
