package cn.rhzhz.mapper;

import cn.rhzhz.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("select * from user where username=#{username}")
    User findByUserName(String username);

//    //添加
//    @Insert("insert into user(username,password,created_time)" +
//            "values(#{username},#{password},now())")
//    void register(String username, String password);

    //添加
    @Insert({
            "INSERT INTO user (username, password, created_time,update_time, weight, height, target_weight, daily_calorie_limit)",
            "VALUES (#{username}, #{password}, NOW(),NOW(), #{weight}, #{height}, #{targetWeight}, #{dailyCalorieLimit})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 捕获自增主键
    void register(User user);

    //更新
    @Update("update user set username=#{username},update_time=#{updateTime},weight=#{weight},height=#{height}," +
            "target_weight=#{targetWeight},daily_calorie_limit=#{dailyCalorieLimit} where id = #{id}")
    void update(User user);


    //更新头像
    @Update("update user set user_pic=#{avatarUrl},update_time=NOW() where id =#{id}")
    void updateAvatar(String avatarUrl, Integer id);

    //更新密码
    @Update("update user set password=#{password} , update_time =NOW() where id = #{id}")
    void updatePwd(String password, Integer id);
}
