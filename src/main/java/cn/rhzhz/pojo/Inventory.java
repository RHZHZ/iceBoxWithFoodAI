package cn.rhzhz.pojo;

import cn.rhzhz.enumType.StorageLocation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Inventory {
    //一个用户有多个冰箱夹层，一个夹层有多种食物
    //一个用户多个食品记录，一条记录属于一个用户，一个食品属于一个夹层


    private int id;
    private User user; // 所属用户
    private int foodId; // 关联食品记录ID

    private StorageLocation storageLocation; // 存储位置夹层


}