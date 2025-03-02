package cn.rhzhz.pojo;

import com.theokanning.openai.service.OpenAiService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class OpenAiServiceWrapper {
    private final OpenAiService openAiService;

    public OpenAiServiceWrapper(String apiKey, String customBaseUrl) {
        this.openAiService = new OpenAiService(apiKey);

        try {
            // 获取私有静态字段 "BASE_URL"
            Field baseUrlField = OpenAiService.class.getDeclaredField("BASE_URL");
            baseUrlField.setAccessible(true);

            // 直接设置字段值（无需修改 final 修饰符）
            baseUrlField.set(null, customBaseUrl); // 静态字段传 null
        } catch (Exception e) {
            throw new RuntimeException("修改 baseUrl 失败", e);
        }
    }

    public OpenAiService getOpenAiService() {
        return openAiService;
    }
}
