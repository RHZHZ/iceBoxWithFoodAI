package cn.rhzhz;

import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.service.FoodInventoryService;
import cn.rhzhz.aiService.ModelService;
import cn.rhzhz.utils.SimpleEncryptor;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JwtTest {

    @Mock
    private ModelService modelService;

    @Mock
    private FoodsMapper foodRecordMapper;

    @InjectMocks
    private FoodInventoryService foodInventoryService;

//    @Test
//    void testProcessConsumption() throws JSONException {
//        // 模拟 AI 响应
//        when(modelService.streamChatCompletion(anyString()))
//                .thenReturn(Flux.just("我吃了200克苹果"));
//
//        // 模拟数据库查询
//        FoodRecord mockFood = new FoodRecord();
//        mockFood.setRemainingQuantity(new BigDecimal("500"));
//        when(foodRecordMapper.findByName(eq("苹果"), anyInt()))
//                .thenReturn(Optional.of(mockFood));
//
//        // 执行测试
//        Flux<String> result = foodInventoryService.processConsumption("test", "deepseek");
//
//        // 验证结果
//
//    }

    @Test
    public void testGen(){
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",1);
        claims.put("username","张");

        //生成JWT代码
        String Token = JWT.create()
                .withClaim("user",claims)//添加载荷
                .withExpiresAt(new Date(System.currentTimeMillis()+1000*60*60*12))//添加过期时间12h
                .sign(Algorithm.HMAC256("rhzhz"));//指定算法配置秘钥

        System.out.println(Token);

    }

    @Test
    public void testParse(){
        String token ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJ1c2VyIjp7ImlkIjoxLCJ1c2VybmFtZSI6IuW8oCJ9LCJleHAiOjE3NDA2MDI2MTZ9." +
                "8kFzKLdQLbc7J38r4jel5-gN7hsF00FI-Hje4uDoYI0";
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("rhzhz"))
                        .build();//生成验证器

        DecodedJWT decodedJWT = jwtVerifier.verify(token);//验证Token,生成一个解析后的JWT对象
        Map<String, Claim> claims = decodedJWT.getClaims();//取得所有载荷
        System.out.println(claims.get("user"));
        System.out.println(claims);

        //如果篡改了头部和载荷部分数据以及秘钥或是过期，都会验证失败
    }

    @Test void testEncoder() throws Exception {
        //加密
        String password = "123456";
//        String sagf= passwordEncoder
        String safePassword = SimpleEncryptor.encrypt(password);
        System.out.println(safePassword);

        // 验证原始密码与加密密码是否匹配
        System.out.println(SimpleEncryptor.decrypt(safePassword));
    }
}
