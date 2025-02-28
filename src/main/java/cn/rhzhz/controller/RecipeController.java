package cn.rhzhz.controller;

import cn.rhzhz.DTO.IngredientVO;
import cn.rhzhz.DTO.RecipeQuery;
import cn.rhzhz.DTO.RecipeResponse;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.pojo.PageResult;
import cn.rhzhz.pojo.RecipeEntity;
import cn.rhzhz.service.RecipeService;
import cn.rhzhz.utils.ThreadLocalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mysql.cj.conf.PropertyKey.logger;

@RestController
@RequestMapping("/recipe")
public class RecipeController {
    private final RecipeService recipeService;
    private static final Logger logger = LoggerFactory.getLogger(FoodsRecordController.class);
    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    //根据需求生成菜谱
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Result<RecipeResponse>> generateRecipe(
            @RequestParam String message,
            @RequestParam(defaultValue = "deepseek") String provider) throws JSONException {

        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        logger.info("用户ID：{}, 请求生成菜谱，模型：{}，提示词：{}", userId, provider, message);

        // 构造结构化提示词模板
        String structuredPrompt = "请根据以下需求生成菜谱：\n"
                + message + "\n"
                + "要求按以下纯文本格式回复，用中文符号：\n"
                + "食材清单：\n"
                + "- 食材1名称: 数量+单位（如：番茄: 2个）\n"
                + "- 食材2名称: 数量+单位\n"
                + "烹饪步骤：\n"
                + "1. 步骤1描述\n"
                + "2. 步骤2描述\n"
                + "菜系：川菜/粤菜等\n"
                + "烹饪时间：X分钟\n"
                + "热量：XXX大卡\n"
                + "健康建议：适度吃/多吃/少吃";
        return recipeService.generateStructuredRecipe(structuredPrompt, provider, userId);
    }

    //分页查询菜谱
    @GetMapping("/list")
    public Result<PageResult<RecipeResponse>> listRecipes(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 构建查询参数
        RecipeQuery query = new RecipeQuery(userId, cuisineType);
        // 分页查询
        PageResult<RecipeEntity> pageResult = recipeService.listRecipes(query, page, size);

        // 转换为响应DTO
        List<RecipeResponse> data = pageResult.getData().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return Result.success(new PageResult<>(
                pageResult.getTotal(),
                pageResult.getPages(),
                data
        ));
    }

    // 实体转响应对象
    public RecipeResponse convertToResponse(RecipeEntity entity) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<IngredientVO> ingredients = mapper.readValue(
                    entity.getIngredientsJson(),
                    new TypeReference<List<IngredientVO>>() {}
            );
            List<String> steps = mapper.readValue(
                    entity.getStepsJson(),
                    new TypeReference<List<String>>() {}
            );

            return new RecipeResponse(
                    ingredients,
                    steps,
                    entity.getCuisineType(),
                    entity.getCookingTime(),
                    entity.getCalories(),
                    entity.getHealthAdvice()
            );
        } catch (JsonProcessingException e) {
            logger.error("菜谱数据解析失败 | ID:{} | 内容:{}", entity.getId(), entity.getIngredientsJson(), e);
            throw new RuntimeException("数据格式错误", e);

        }
    }
}
