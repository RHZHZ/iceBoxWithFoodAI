package cn.rhzhz.service;

import cn.rhzhz.config.AIConfig;
import cn.rhzhz.controller.FoodsRecordController;
import cn.rhzhz.mapper.ConfigMapper;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class ConfigService {

    @Autowired
    ConfigMapper configMapper;

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);


    public AIConfig getConfig() {
        //1.获取配置
        AIConfig config = null;
        try {
            config = configMapper.getConfig();
            //2.判断
            if (config.getId()==1) {
                logger.info("配置信息：{}", config);
            }
            else{
                logger.warn("请配置数据库aiConfig");
                return null;
            }
            // 提示词优化确保参考日期计算正确
            String old_prompt = config.getPrompt();
            String head_date ="今天的日期是:" + String.valueOf(LocalDate.now()) ;
            String new_prompt = head_date + old_prompt;
            config.setPrompt(new_prompt);
            logger.info("提示词优化,日期：{},提示词:{}", head_date,new_prompt);
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}