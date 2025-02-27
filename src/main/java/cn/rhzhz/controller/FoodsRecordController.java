package cn.rhzhz.controller;

import cn.rhzhz.service.FoodInventoryService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.rhzhz.dto.FoodRecordDTO;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.pojo.Result;
import cn.rhzhz.service.FoodsRecordService;
import cn.rhzhz.utils.ThreadLocalUtil;
import jakarta.validation.Valid;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/foods")
public class FoodsRecordController {
    @Autowired
    private FoodsRecordService foodsRecordService;
    @Autowired
    private FoodInventoryService foodInventoryService;
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
    public Result addRecord(@Valid @RequestBody FoodRecordDTO foodDTO, BindingResult result) {

        logger.info("接收到添加食品记录请求: {}", foodDTO);

        // 1. 校验参数合法性
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return Result.error(String.valueOf(errors));
        }

        // 2. 获取用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        int userId = (int) map.get("id");

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
        if (foodRecordList.size()>0){
            return Result.success(foodRecordList);
        }else {
            return Result.error("未找到记录");
        }

    }

    //获取临期产品 7Days
    @GetMapping("/expiringFoods")
    public Result getExpiringFoods() {
        List<FoodRecord> foodRecordList = foodsRecordService.getExpiringFoodRecords();
        if (foodRecordList.size()>0){
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

    @PostMapping("/consume")
    public Result consumeFood(@RequestParam int foodId, @RequestParam BigDecimal amount) {
        try {
            FoodRecord updatedFood = foodsRecordService.consumeFood(foodId, amount);
            return Result.success( updatedFood);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    // 新增端点：处理自然语言输入的食物消耗
    @PostMapping(value = "/consumeAi", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> consumeFood(@RequestParam String message,@RequestParam(defaultValue = "deepseek") String provider) throws JSONException {
        logger.info("AI处理信息：｛｝",message);
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        message+="请不要回复其他的东西按照以下格式回复：食物名称：消耗数量。如：全麦面包:100";
        return foodInventoryService.processConsumption(message, provider,id);
    }

}
