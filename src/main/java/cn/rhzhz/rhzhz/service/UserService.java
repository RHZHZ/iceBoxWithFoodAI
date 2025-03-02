package cn.rhzhz.service;

import cn.rhzhz.pojo.User;

public interface UserService {
    //根据用户名查询用户
    User findByUserName(String username);
    //注册
//    void register(String username, String password);
    void register(User users);
    //更新
    void update(User user);

    //更新头像
    void updateAvatar(String avatarUrl);

    //更新密码
    void updatePwd(String password);
}
