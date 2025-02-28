package cn.rhzhz.enumType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteEntity {
    private Long id;
    private Integer userId;
    private Long recipeId;
    private LocalDateTime favoriteTime;
}
