package cn.rhzhz.service.impl;

import cn.rhzhz.DTO.IngredientVO;
import cn.rhzhz.DTO.RecipeQuery;
import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;

import cn.rhzhz.aiService.ModelServiceFactory;
import cn.rhzhz.controller.FoodsRecordController;
import cn.rhzhz.mapper.RecipeMapper;
import cn.rhzhz.pojo.PageResult;
import cn.rhzhz.pojo.RecipeEntity;
import cn.rhzhz.service.RecipeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService {
    @Autowired
    private  ModelServiceFactory modelServiceFactory; // 确保已注入
    @Autowired
    private RecipeMapper recipeMapper;

    private static final Logger logger = LoggerFactory.getLogger(FoodsRecordController.class);



    @Override
    @Transactional
    public Flux<Result<RecipeResponse>> generateStructuredRecipe(String prompt, String provider, int userId) throws JSONException {
        return modelServiceFactory.getService(provider)
                .streamChatCompletion(prompt)
                .collectList()
                .flatMapMany(responseList -> {
                    String rawText = String.join("", responseList);
                    logger.info("原始AI响应：{}", rawText);
                    try {
                        // 解析文本数据
                        RecipeResponse response = parseTextResponse(rawText);
                        //必填字段校验
                        validateResponse(response);

                        // 持久化到数据库
                        RecipeEntity entity = convertToEntity(response, userId);
                        // 持久化到数据库
                        recipeMapper.insertRecipe(entity);

                        return Flux.just(Result.success(response));

                    } catch (Exception e) {
                        logger.error("菜谱解析失败 | 原始内容：{} | 错误：{}", rawText, e.getMessage());
                        return Flux.just(Result.error("解析失败: " + e.getMessage()));
                    }
                });
    }

    @Override
    public PageResult<RecipeEntity> listRecipes(RecipeQuery query, int page, int size) {
        // MyBatis分页插件
        PageHelper.startPage(page, size);

        // 动态SQL查询
        List<RecipeEntity> recipes = recipeMapper.selectByQuery(query);

        // 获取分页信息
        PageInfo<RecipeEntity> pageInfo = new PageInfo<>(recipes);
        return new PageResult<>(
                pageInfo.getTotal(),
                pageInfo.getPages(),
                recipes
        );
    }

    /**
     * 解析纯文本响应
     */
    private RecipeResponse parseTextResponse(String rawText) {
        RecipeResponse response = new RecipeResponse();

        // 解析食材清单（示例：食材清单：\n- 番茄: 2个\n- 鸡蛋: 3个）
        if (rawText.contains("食材清单：")) {
            String ingredientsSection = rawText.split("食材清单：")[1].split("烹饪步骤：")[0];
            List<IngredientVO> ingredients = Arrays.stream(ingredientsSection.split("\n"))
                    .filter(line -> line.trim().startsWith("- "))
                    .map(line -> {
                        String[] parts = line.replace("- ", "").split(":");
                        return new IngredientVO(parts[0].trim(), parts[1].trim());
                    })
                    .collect(Collectors.toList());
            response.setIngredients(ingredients);
        }

        // 解析烹饪步骤（示例：烹饪步骤：\n1. 切番茄\n2. 炒鸡蛋）
        if (rawText.contains("烹饪步骤：")) {
            String stepsSection = rawText.split("烹饪步骤：")[1];
            List<String> steps = Arrays.stream(stepsSection.split("\n"))
                    .filter(line -> line.matches("\\d+\\..+"))
                    .map(line -> line.replaceFirst("\\d+\\.", "").trim())
                    .collect(Collectors.toList());
            response.setSteps(steps);
        }

        // 解析其他字段（正则匹配）
        response.setCuisineType(extractByRegex(rawText, "菜系：(.+?)\n"));
        response.setCookingTime(extractByRegex(rawText, "烹饪时间：(.+?)\n"));
        response.setCalories(extractByRegex(rawText, "热量：(.+?)大卡"));
        response.setHealthAdvice(extractByRegex(rawText, "健康建议：(.+?)(\n|$)"));

        //必填字段校验
//        validateResponse(response);
        logger.info("解析AI响应：{}", response);
        return response;
    }

    /**
     * 正则提取字段
     */
    private String extractByRegex(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private void validateResponse(RecipeResponse response) {
        if (response.getIngredients() == null || response.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("食材清单不能为空");
        }
        if (response.getSteps() == null || response.getSteps().isEmpty()) {
            throw new IllegalArgumentException("烹饪步骤不能为空");
        }
    }

    // DTO转Entity
    private RecipeEntity convertToEntity(RecipeResponse dto, Integer userId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        RecipeEntity recipeEntity =new RecipeEntity();
        recipeEntity.setUserId(userId);
        recipeEntity.setCalories(dto.getCalories());
        recipeEntity.setCuisineType(dto.getCuisineType());
        recipeEntity.setHealthAdvice(dto.getHealthAdvice());
        recipeEntity.setCookingTime(dto.getCookingTime());
        // 正确序列化为JSON字符串
        recipeEntity.setIngredientsJson(mapper.writeValueAsString(dto.getIngredients()));
        recipeEntity.setStepsJson(mapper.writeValueAsString(dto.getSteps()));
        return recipeEntity;
    }



}
