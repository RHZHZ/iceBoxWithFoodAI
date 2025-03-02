package cn.rhzhz.aiService;

import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeepSeekService implements ModelService {

    @Value("${ai.providers.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.providers.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.providers.deepseek.model}")
    private String model;

    @Override
    public Flux<String> streamChatCompletion(String message) throws JSONException {
        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = buildRequestBody(message);
        System.out.println("请求体: " + new JSONObject(requestBody).toString(2)); // 打印格式化JSON

        return client.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> System.out.println("原始响应块: " + chunk)) // 打印原始响应
                .map(this::extractContentFromResponse);
    }

    private Map<String, Object> buildRequestBody(String message) {
        return Map.of(
                "model", model,
                "messages", Collections.singletonList(Map.of(
                        "role", "user",
                        "content", message
                )),
                "stream", true,
                "temperature", 0.7,      // 必填参数
                "max_tokens", 1000       // 必填参数
        );
    }

    private String extractContentFromResponse(String chunk) {
        try {
            // 处理可能的空内容（如心跳包）
            if (chunk.contains("\"delta\":{}")) return "";
            return JsonPath.read(chunk, "$.choices[0].delta.content");
        } catch (Exception e) {
            System.err.println("解析失败，原始响应: " + chunk);
            return "";
        }
    }
}