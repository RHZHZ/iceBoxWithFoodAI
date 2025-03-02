package cn.rhzhz.service.impl;

import cn.rhzhz.mapper.UserMapper;
import cn.rhzhz.pojo.User;
import cn.rhzhz.service.UserService;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String username) {
        User user = userMapper.findByUserName(username);
        return user;
    }

//    @Override
//    public void register(String username, String password) {
//        //加密
//        String safePassword = password;
//        //添加
//        userMapper.register(username,safePassword);
//    }

    @Override
    public void register(User users) {
        //加密
        String safePassword = users.getPassword();
        users.setPassword(safePassword);
        //添加
        userMapper.register(users);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl,id);
    }

    @Override
    public void updatePwd(String password) {

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(password,id);
    }

}
