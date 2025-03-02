package cn.rhzhz.controller;
import cn.rhzhz.mapper.FoodsMapper;
import cn.rhzhz.pojo.FoodTypeCount;
import cn.rhzhz.service.ConfigService;
import cn.rhzhz.service.FoodInventoryService;
import cn.rhzhz.utils.FoodRecordTools;
import jakarta.validation.ValidationException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.rhzhz.DTO.FoodRecordDTO;
import cn.rhzhz.pojo.FoodRecord;
import cn.rhzhz.DTO.Result;
import cn.rhzhz.service.FoodsRecordService;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/foods")
public class FoodsRecordController {
    @Autowired
    private FoodsRecordService foodsRecordService;
    @Autowired
    private FoodInventoryService foodInventoryService;

    @Autowired
    ConfigService configService;

    FoodRecordTools foodRecordTools = new FoodRecordTools();
    @Autowired
    private FoodsMapper foodsMapper;


    private static final Logger logger = LoggerFactory.getLogger(FoodsRecordController.class);

    //获取用户食物记录
    @GetMapping("/foodRecordList")
    public Result<List<FoodRecord>> list(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getFoodList());
    }
    //获取用户吃完的食物记录
    @GetMapping("/foodEatRecordList")
    public Result<List<FoodRecord>> foodEatRecordList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getEatFoodList());
    }
    //获取用户正常的食物记录
    @GetMapping("/foodNormalRecordList")
    public Result<List<FoodRecord>> getNormalFoodList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getNormalFoodList());
    }
    //获取用户浪费的食物记录
    @GetMapping("/foodThrowRecordList")
    public Result<List<FoodRecord>> getThrowFoodList(/*@RequestHeader(name = "Authorization") String token, HttpServletResponse response*/){

        return Result.success(foodsRecordService.getThrowFoodList());
    }


    //增添一条记录
    @Transactional
    @PostMapping("/addRecord")
    public Result addRecord(@RequestBody @Validated FoodRecordDTO foodDTO) {
        logger.info("接收到添加食品记录请求: {}", foodDTO);
        // 2. 获取用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        int userId = (int) map.get("id");
        foodDTO.setUserId(userId);

        //2.1 确保食品唯一
        List<FoodRecord> existing = foodsMapper.findByName(foodDTO.getName(), userId);
        if (!existing.isEmpty()) {

            return Result.error("食品已存在");
        }
        // 3. 将 DTO 转换为实体类
        FoodRecord foodRecord = new FoodRecord().convertToEntity(foodDTO, userId);
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

    //修改一条记录
    //增添一条记录
    @Transactional
    @PutMapping("/updateRecord")
    public Result updateRecord(@RequestBody @Validated FoodRecordDTO foodDTO) {
        logger.info("接收到修改食品记录请求: {}", foodDTO);

        //核实ID
        Map<String, Object> map = ThreadLocalUtil.get();
        int userId = (int) map.get("id");
        if(foodDTO.getUserId()!=userId)return Result.error("违规操作");
        //根据ID查
        try {
            //查找旧的记录
            FoodRecord oldFoodRecord = foodsMapper.findById(foodDTO.getId());
             if(oldFoodRecord==null){ return Result.error("不存在的记录");}
            logger.info("oldFoodRecord: {}", oldFoodRecord);

            FoodRecord newFoodRecord = new FoodRecord().convertToEntity(foodDTO,foodDTO.getUserId());
            //补充属性
            newFoodRecord.setId(foodDTO.getId());
            logger.info("映射修改食品记录请求: {}", newFoodRecord);
            foodsRecordService.updateRecord(newFoodRecord);
            return Result.success("食品记录添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

    }

    //获取用户浪费foods总价格
    @GetMapping("/getTotalPriceOfThrownFood")
    public Result getTotalPriceOfThrownFood(){

        return Result.success(foodsRecordService.getTotalPriceOfThrownFood());
    }

    //根据分类获取食物记录
    @PatchMapping("/getFoodRecordBytype")
    public Result<List<FoodRecord>> getFoodRecordBytype(@RequestParam String type){
        List<FoodRecord> foodRecordList = foodsRecordService.getFoodRecordBytype(type);
        if (!foodRecordList.isEmpty()){
            return Result.success(foodRecordList);
        }else {
            return Result.error("未找到记录");
        }

    }



    //获取临期产品 7Days
    @GetMapping("/expiringFoods")
    public Result<List<FoodRecord>> getExpiringFoods() {
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
    public Result<List<FoodRecord>> searchByName(@RequestParam String name) {
        List<FoodRecord> records = foodsRecordService.searchByName(name);
        return Result.success(records);
    }

    //统计分类返回记录
    @PatchMapping("/groupByType")
    public Result<List<FoodTypeCount>> groupByType(){
        List<FoodTypeCount> foodTypeCounts = foodsRecordService.groupByType();
        return Result.success(foodTypeCounts);
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
    @Transactional
    @PostMapping(value = "/recordByAi", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> recordByAi(
            @RequestParam String message,
            @RequestParam(defaultValue = "deepseek") String provider) {

        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        logger.info("用户ID：{}, 通过AI模型:{}, 处理信息：{}", userId, provider, message);

        // 构造AI提示词
        ////1.获取提示词
        String propmt_fromDataBase = configService.getConfig().getPrompt();
        ////2.构造完整提示词
        String prompt = String.format(propmt_fromDataBase,message, userId);
        ////3.处理
        return foodInventoryService.callAIProvider(prompt, provider)
                .collectList()
                .flatMapMany(aiResponses -> {
                    String fullResponse = String.join("", aiResponses);
                    List<FoodRecordDTO> dtos = null;
                    try {
                        dtos = foodRecordTools.parseAIResponse(fullResponse, userId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return validateAndSave(dtos, userId)
                            .onBackpressureBuffer() // 处理背压
                            .doOnNext(msg -> logger.info("处理结果: {}", msg));
                });
    }

    // 校验保存逻辑
    public Flux<String> validateAndSave(List<FoodRecordDTO> dtos, Integer userId) {

        return Flux.fromIterable(dtos)
                .flatMap(dto ->
                        Mono.fromCallable(() -> {
                                    // 参数校验逻辑
                                    List<String> errors = foodRecordTools.validateDTO(dto);
                                    if (!errors.isEmpty()) {
                                        throw new ValidationException(String.join(", ", errors));
                                    }

                                    // 唯一性校验
                                    boolean exists = foodsRecordService.existsByUserAndNameAndDate(
                                            userId,
                                            dto.getName(),
                                            dto.getPurchaseDate()
                                    );
                                    if (exists) {
                                        throw new DuplicateKeyException("重复记录: " + dto.getName());
                                    }

                                    // 转换并保存
                                    FoodRecord record = new FoodRecord();
                                    record = record.convertToEntity(dto, userId);
                                    foodsRecordService.addRecord(record);
                                    return "成功添加记录: " + dto.getName();
                                })
                                .onErrorResume(e -> {
                                    String errorMsg = e instanceof ValidationException ? e.getMessage() : "系统错误";
                                    return Mono.just("添加失败[" + dto.getName() + "]: " + errorMsg);
                                })
                );
    }


}
