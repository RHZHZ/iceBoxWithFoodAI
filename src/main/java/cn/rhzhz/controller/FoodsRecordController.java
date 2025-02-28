package cn.rhzhz.controller;

import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.service.FoodInventoryService;
import cn.rhzhz.utils.ImageApiClient;
import cn.rhzhz.utils.SimplifiedBindingResult;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.rhzhz.DTO.FoodRecordDTO;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.service.FoodsRecordService;
import cn.rhzhz.utils.ThreadLocalUtil;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/foods")
public class FoodsRecordController {
    @Autowired
    private FoodsRecordService foodsRecordService;
    @Autowired
    private FoodInventoryService foodInventoryService;

    @Autowired
    private FoodsMapper foodsMapper;


    private static final Logger logger = LoggerFactory.getLogger(FoodsRecordController.class);

    //获取用户食物记录
    @GetMapping("/foodRecordList")
    public Result list(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getFoodList());
    }
    //获取用户吃完的食物记录
    @GetMapping("/foodEatRecordList")
    public Result foodEatRecordList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getEatFoodList());
    }
    //获取用户正常的食物记录
    @GetMapping("/foodNormalRecordList")
    public Result getNormalFoodList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getNormalFoodList());
    }
    //获取用户浪费的食物记录
    @GetMapping("/foodThrowRecordList")
    public Result getThrowFoodList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getThrowFoodList());
    }


    //增添一条记录
    @PostMapping("/addRecord")
    public Result addRecord(@Valid @RequestBody FoodRecordDTO foodDTO) {

        logger.info("接收到添加食品记录请求: {}", foodDTO);


        // 1. 校验参数合法性
//        if (result.hasErrors()) {
//            List<String> errors = result.getFieldErrors()
//                    .stream()
//                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
//                    .toList();
//            return Result.error(String.valueOf(errors));
//        }

        // 2. 获取用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        int userId = (int) map.get("id");

        //2.1 确保食品唯一
        List<FoodRecord> existing = foodsMapper.findByName(foodDTO.getName(), userId);
        if (!existing.isEmpty()) {

            return Result.error("食品已存在");
        }

        // 3. 将 DTO 转换为实体类
        FoodRecord foodRecord = new FoodRecord();
        foodRecord.setUserId(userId);
        foodRecord.setName(foodDTO.getName());
        foodRecord.setImgUrl(foodDTO.getImgUrl());
        foodRecord.setExpirationDate(foodDTO.getExpirationDate());
        foodRecord.setPurchaseDate(foodDTO.getPurchaseDate());
        foodRecord.setPrice(foodDTO.getPrice());
        foodRecord.setUnitType(foodDTO.getUnitType());
        foodRecord.setType(foodDTO.getType());
        foodRecord.setTotalQuantity(foodDTO.getTotalQuantity());
        foodRecord.setRemainingQuantity(foodDTO.getRemainingQuantity());
//        foodRecord.setBatchTag(foodDTO.getBatchTag());
        foodRecord.setInfo(foodDTO.getInfo());
        logger.info("映射后到添加食品记录请求: {}", foodRecord);

        // 4. 调用服务层方法
        foodsRecordService.addRecord(foodRecord);

        return Result.success("食品记录添加成功");
    }

    //删除一条记录
    @PatchMapping("/delRecord")
    public Result delRecord(@RequestParam int foodRecordId){
        try {
            foodsRecordService.delRecord(foodRecordId);
            return Result.success();
        } catch (Exception e) {
           return Result.error(String.valueOf(e));
        }

    }

    //获取用户浪费foods总价格
    @GetMapping("/getTotalPriceOfThrownFood")
    public Result getTotalPriceOfThrownFood(){

        return Result.success(foodsRecordService.getTotalPriceOfThrownFood());
    }

    //根据分类获取食物记录
    @PatchMapping("/getFoodRecordBytype")
    public Result getFoodRecordBytype(@RequestParam String type){
        List<FoodRecord> foodRecordList = foodsRecordService.getFoodRecordBytype(type);
        if (!foodRecordList.isEmpty()){
            return Result.success(foodRecordList);
        }else {
            return Result.error("未找到记录");
        }

    }

    //获取临期产品 7Days
    @GetMapping("/expiringFoods")
    public Result getExpiringFoods() {
        List<FoodRecord> foodRecordList = foodsRecordService.getExpiringFoodRecords();
        if (!foodRecordList.isEmpty()){
            logger.info("临期foodRecordList:{}",foodRecordList);
            return Result.success(foodRecordList);
        }else {
            return Result.error("未找到记录");
        }

    }

    //模糊查询
    @PatchMapping("/search")
    public Result searchByName(@RequestParam String name) {
        List<FoodRecord> records = foodsRecordService.searchByName(name);
        return Result.success(records);
    }

    //食物消耗
    @PostMapping("/eat")
    public Result consumeFood(@RequestParam int foodId, @RequestParam BigDecimal amount) {
        try {
            FoodRecord updatedFood = foodsRecordService.consumeFood(foodId, amount);
            return Result.success( updatedFood);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    // 新增端点：处理自然语言输入的食物消耗
    @PostMapping(value = "/consumeByAi", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public  Flux<String> consumeFood(
            @RequestParam String message,

            @RequestParam(defaultValue = "deepseek") String provider) throws JSONException {

        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        logger.info("用户ID：{},通过AI模型:{},处理信息：{}", id, provider, message);

        // 拼接提示词
        message += "用户id:" + id;
        message += "请不要回复其他内容，按格式回复：用户id:10;食物名称：消耗数量;如：用户id:7;全麦面包:100克";

        return foodInventoryService.processConsumption(message, provider, id);

    }

    // 新增端点：处理自然语言输入的食物记录
    @PostMapping(value = "/recordByAi", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> recordByAi(
            @RequestParam String message,
            @RequestParam(defaultValue = "deepseek") String provider) {

        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        logger.info("用户ID：{}, 通过AI模型:{}, 处理信息：{}", userId, provider, message);

        // 构造AI提示词
        String prompt = String.format("""
        用户输入：%s
        用户ID：%d
        请生成食品记录，格式要求，未说明的情况请你根据一般情况补充：
        名称：[标准名称]
        类型：[分类]
        购买日期：[YYYY-MM-DD]
        保质期：[YYYY-MM-DD]
        总量：[数值+单位]
        价格：[数值]
        单位类型：[克/毫升/个]
        图片：[URL]
        示例：
        名称:牛奶
        类型:乳制品
        购买日期:2024-03-01
        保质期:2024-03-08
        总量:1000毫升
        价格:12.5
        单位类型:毫升
        图片:https://example.com/milk.jpg
        """, message, userId);

        return foodInventoryService.callAIProvider(prompt, provider)
                .collectList()
                .flatMapMany(aiResponses -> {
                    String fullResponse = String.join("", aiResponses);
                    logger.info("AI完整响应: {}", fullResponse);

                    try {
                        List<FoodRecordDTO> dtos = parseAIResponse(fullResponse, userId);
                        return validateAndSave(dtos,userId);
                    } catch (Exception e) {
                        return Flux.just("错误: " + e.getMessage());
                    }
                });
    }

    // 校验保存逻辑
    private Flux<String> validateAndSave(List<FoodRecordDTO> dtos, Integer userId) {
        SimplifiedBindingResult results = new SimplifiedBindingResult();
        return Flux.fromIterable(dtos)
                .flatMap(dto -> {

                    List<String> errors = new ArrayList<>();

                    // 手动设置用户ID
                    dto.setUserId(userId);

                    // 手动验证字段
                    if (dto.getName() == null || dto.getName().isBlank()) {
                        errors.add("名称不能为空");
                    }
                    if (dto.getTotalQuantity() == null || dto.getTotalQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("总量必须大于0");
                    }
                    if (dto.getExpirationDate().isBefore(dto.getPurchaseDate())) {
                        errors.add("保质期不能早于购买日期");
                    }

                    if (!errors.isEmpty()) {
                        return Flux.just("校验失败: " + String.join(", ", errors));
                    }

                    try {
                        // 复用原有添加逻辑
                        // 3. 将 DTO 转换为实体类
                        FoodRecord foodRecord = new FoodRecord();
                        foodRecord.setUserId(userId);
                        foodRecord.setName(dto.getName());
                        foodRecord.setImgUrl(dto.getImgUrl());
                        foodRecord.setExpirationDate(dto.getExpirationDate());
                        foodRecord.setPurchaseDate(dto.getPurchaseDate());
                        foodRecord.setPrice(dto.getPrice());
                        foodRecord.setUnitType(dto.getUnitType());
                        foodRecord.setType(dto.getType());
                        foodRecord.setTotalQuantity(dto.getTotalQuantity());
                        foodRecord.setRemainingQuantity(dto.getRemainingQuantity());
                        foodRecord.setInfo(dto.getInfo());
                        //ADD
                        logger.info("语言处理映射后到添加食品记录请求: {}", foodRecord);
                        try {
                            foodsRecordService.addRecord(foodRecord);
                            return Flux.just("成功添加记录: " + dto.getName());
                        } catch (Exception e) {
                            return Flux.just("添加失败: " + e.getMessage());

                        }
                    } catch (RuntimeException e) {
                        return Flux.just("添加失败: " + e.getMessage());
                    }
                });

    }

    // 解析逻辑-支持多条目处理
    private List<FoodRecordDTO> parseAIResponse(String response, Integer userId) throws Exception {

        List<FoodRecordDTO> records = new ArrayList<>();

        // 按条目分割（支持数字编号或项目符号）
        String[] items = response.split("\\d+\\. |\\* ");

        // 实现解析逻辑
        for (String item : items) {
            if (item.trim().isEmpty()) continue;

            Map<String, String> data = new HashMap<>();
            Pattern pairPattern = Pattern.compile("(名称|类型|购买日期|保质期|总量|价格|单位类型|图片)[:：]\\s*(.+)");

            Matcher matcher = pairPattern.matcher(item);
            while (matcher.find()) {
                data.put(matcher.group(1).trim(), matcher.group(2).trim());
            }
            if (data.containsKey("名称")) {
                FoodRecordDTO dto = new FoodRecordDTO();

                try {
                    dto.setUserId(userId);
                    dto.setName(data.getOrDefault("名称", ""));
                    dto.setTotalQuantity(parseQuantity(data.get("总量")));
                    dto.setRemainingQuantity(dto.getTotalQuantity()); // 剩余量等于总量
                    dto.setUnitType(data.get("单位类型"));
                    dto.setType(data.get("类型"));
                    dto.setPrice(parseQuantity(data.get("价格")));

                    //根据文字获取一张图片
                    ImageApiClient imageApiClient = new ImageApiClient();
                    String pic = imageApiClient.getImageUrls(data.getOrDefault("名称", "AI"));
                    dto.setImgUrl(pic);
                    logger.info("获取到图片：｛｝",pic);

                    dto.setInfo(0); // 默认正常状态
                } catch (Exception e) {
                    logger.error("parseAIResponseError：{}",e);
                    throw new RuntimeException(e);
                }

                // 日期处理
                try {
                    dto.setExpirationDate(LocalDate.parse(data.get("保质期")));
                } catch (Exception e) {
                    throw new IllegalArgumentException("保质期格式错误");
                }

                if (data.containsKey("购买日期")) {
                    try {
                        dto.setPurchaseDate(LocalDate.parse(data.get("购买日期")));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("购买日期格式错误");
                    }
                } else {
                    dto.setPurchaseDate(LocalDate.now());
                }
                //add a record
                records.add(dto);
            }
        }
        return records;
    }

    private BigDecimal parseQuantity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return BigDecimal.ZERO; // 或抛 IllegalArgumentException
        }

        // 保留数字、负号、小数点，替换逗号为点
        String numStr = input.replaceAll("[^0-9.-]", "")
                .replace(",", ".");

        // 处理多小数点或无效格式
        if (numStr.isEmpty() || numStr.matches(".*\\..*\\..*")) {
            return BigDecimal.ZERO;
        }

        // 清理符号和格式
        numStr = numStr.replaceAll("(?<!^)-", ""); // 只允许开头的负号
        numStr = numStr.replaceAll("^(-?)0+(\\d+)", "$1$2"); // 移除前导零

        try {
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO; // 或抛自定义异常
        }
    }

}
