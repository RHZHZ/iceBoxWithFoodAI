package cn.rhzhz.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBean<T> {
    private long total;       // 总记录数
    private int pages;        // 总页数
    private List<T> items;     // 当前页数据

}
