package cn.rhzhz.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class ModelServiceFactory {

    private final Map<String, ModelService> services;

    public ModelServiceFactory(OpenAIService openAIService, DeepSeekService deepSeekService) {
        services = Map.of(
                "openai", openAIService,
                "deepseek", deepSeekService
        );
    }

    public ModelService getService(String provider) {
        return Optional.ofNullable(services.get(provider))
                .orElseThrow(() -> new IllegalArgumentException("不支持的模型服务: " + provider));
    }
}