package cn.rhzhz.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodsCategory {
    private int id;//主键
    @NotEmpty(message = "分类名称不能为空")
    private String categoryName;//分类名称
    private String categoryAlias;//分类别名
    @NotNull(message = "会话已过期")
    private int createUser;//创建人ID
//    @JsonFormat(pattern = "yyyy-MM-dd HH-MM-ss")
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
