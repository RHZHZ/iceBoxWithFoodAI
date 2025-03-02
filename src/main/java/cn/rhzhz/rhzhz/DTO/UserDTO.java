package cn.rhzhz.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在3-50个字符之间")
    @Pattern(regexp = "^\\S+$", message = "用户名不能包含空格")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在6-100个字符之间")
    private String password;

    @DecimalMin(value = "0.0", message = "体重不能小于0")
    @DecimalMax(value = "500.0", message = "体重不能超过500公斤")
    private BigDecimal weight;

    @Min(value = 0, message = "身高不能为负数")
    @Max(value = 300, message = "身高不能超过300厘米")
    private Integer height;

    @DecimalMin(value = "0.0", message = "目标体重不能小于0")
    @DecimalMax(value = "500.0", message = "目标体重不能超过500公斤")
    private BigDecimal targetWeight;

    @Min(value = 0, message = "每日热量限制不能为负数")
    private Integer dailyCalorieLimit;

    // Getters and Setters
}