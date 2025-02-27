package cn.rhzhz.controller;

import cn.rhzhz.pojo.Result;
import cn.rhzhz.pojo.User;
import cn.rhzhz.dto.UserDTO;
import cn.rhzhz.service.UserService;
import cn.rhzhz.utils.JwtUtil;
import cn.rhzhz.utils.SimpleEncryptor;
import cn.rhzhz.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    @PostMapping("/register")
//    public Result register(@Pattern(regexp = "^\\S{3,16}$") String username,@Pattern(regexp = "^\\S{3,16}$") String password){
//        //查询用户
//        User user = userService.findByUserName(username);
//        if(user==null){
//            //没有占有
//
//            //注册
//            userService.register(username,password);
//            return Result.success();
//        }else {
//            //占用
//            return Result.error("用户名已被占用");
//        }
//
//    }
    @PostMapping("/register")
    public Result register(@Valid UserDTO userDTO, BindingResult result) throws Exception {
            // 1. 校验参数合法性
        if (result.hasErrors()) {
            return Result.error(result.getFieldError().getDefaultMessage());
        }
        User user = userService.findByUserName(userDTO.getUsername());
            // 2. 检查用户名是否重复（需调用Service）
        if (user==null) {
            //  3. 密码加密存储
//            String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
            String encryptedPassword = SimpleEncryptor.encrypt(userDTO.getPassword());
            // 4. 转换为User实体并保存
            User users = new User();
            users.setUsername(userDTO.getUsername());
            users.setPassword(encryptedPassword);
            users.setWeight(userDTO.getWeight());
            users.setHeight(userDTO.getHeight());
            users.setTargetWeight(userDTO.getTargetWeight());
            users.setDailyCalorieLimit(userDTO.getDailyCalorieLimit());
            //注册
            userService.register(users);

            return Result.success("注册成功");

        }else {
            //占用
            return Result.error("用户名已存在");
        }
    }

    @PostMapping("/login")
    public Result<String> login( @Valid UserDTO userDTO, BindingResult result) throws Exception {
        // 1. 校验参数合法性
        if (result.hasErrors()) {
            return Result.error(result.getFieldError().getDefaultMessage());
        }
        System.out.println(userDTO);
        //根据用户名查询用户
        User loginUser = userService.findByUserName(userDTO.getUsername());
        //判断用户是否存在
        if(loginUser == null){
            return Result.error("用户名错误");
        }
        //判断密码是否正确
        if(userDTO.getPassword().equals(SimpleEncryptor.decrypt(loginUser.getPassword()))){
            //登录成功
            Map<String,Object> claims = new HashMap<>();
            claims.put("id",loginUser.getId());
            claims.put("username",loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader(name = "Authorization") String token*/){
        //根据用户名查询用户
//        Map<String,Object> map = JwtUtil.parseToken(token);
//        String username = (String) map.get("username");
        Map<String,Object> map = ThreadLocalUtil.get();
        User user = userService.findByUserName((String) map.get("username"));
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        try {

            userService.update(user);
            return Result.success();

        } catch (Exception e) {

            return Result.error("参数错误");

        }

    }

    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params) throws Exception {
        //1.校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) ||! StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要参数");
        }

        //校验原密码是否正确
        //根据用户名拿原密码
        Map<String,Object> map = ThreadLocalUtil.get();
        String username =(String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if (!loginUser.getPassword().equals(SimpleEncryptor.encrypt(oldPwd))){
            return Result.error("原密码错误");
        }

        //新密码和重复密码是否一致
        if(!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一致");
        }

        //2.调用service完成密码更新
        //先检验参数是否合格，再加密
        if(!(newPwd.length()>=5 && newPwd.length() <= 100)){
            return Result.error("请检查密码长度：5-100位");
        }
        String safePwd = SimpleEncryptor.encrypt(newPwd);
        userService.updatePwd(safePwd);
        return Result.success();
    }

}
