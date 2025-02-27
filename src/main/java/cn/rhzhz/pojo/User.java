package cn.rhzhz.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    @NotNull
    private int id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在3-50个字符之间")
    @Pattern(regexp = "^\\S+$", message = "用户名不能包含空格")
    private String username;

    @JsonIgnore
    private String password;
    private String email;

    @DecimalMin(value = "0.0", message = "体重不能小于0")
    @DecimalMax(value = "500.0", message = "体重不能超过500公斤")
    private BigDecimal weight;

    @Min(value = 0, message = "身高不能为负数")
    @Max(value = 300, message = "身高不能超过300厘米")
    private int height;

    @DecimalMin(value = "0.0", message = "目标体重不能小于0")
    @DecimalMax(value = "500.0", message = "目标体重不能超过500公斤")
    private BigDecimal targetWeight;

    @Min(value = 0, message = "每日热量限制不能为负数")
    private int dailyCalorieLimit;
    private LocalDateTime createdTime;
    private LocalDateTime updateTime;
    @URL
    private String avatarUrl;
}
