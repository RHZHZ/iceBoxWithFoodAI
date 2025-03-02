package cn.rhzhz.config;

import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

// AI配置实体类
@Data
public class AIConfig extends BaseConfig {
    private int id;
    private String providerName;
    private String prompt;
    private String model;
    private String apiKey;
    private String baseUrl;
    private String config_info;
}