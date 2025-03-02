# 智能食物管理系统

基于AI和Spring Boot的智能食物管理系统，提供自然语言处理、菜谱生成、库存管理及用户收藏功能。

## 目录
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [安装步骤](#安装步骤)
- API文档
  - [食物记录](#食物记录接口)
  - [菜谱管理](#菜谱管理接口)
  - [用户收藏](#用户收藏接口)
  - [用户管理](#用户管理接口)
- [数据库设计](#数据库设计)
- [注意事项](#注意事项)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 功能特性（部分）
| 模块         | 功能描述                                                     |
| ------------ | ------------------------------------------------------------ |
| AI食物识别   | 解析自然语言输入（如"吃了300g全麦面包"）并更新库存           |
| AI食物记录   | 解析自然语言输入（如"我今天买了500克牛肉，花了50元，属于生鲜类，保质期3天"）并更新库存 |
| 智能菜谱生成 | 根据食材生成包含营养成分和步骤的菜谱(如"生成番茄炒蛋菜谱,我最近减肥") |
| 库存浪费情况 | 支持查询库存食物总浪费金额                                   |
| 用户收藏系统 | 支持菜谱收藏/取消收藏，带分页查询功能                        |
| 库存管理     | 实时追踪食物库存状态，支持多单位换算                         |
| ...          | ...                                                          |

## 技术栈
**后端框架**
- Spring Boot 3.1.5
- MyBatis 3.0.3
- Reactor 3.4.12 (响应式编程)
- webflux
- Openai-gpt3-java 0.18.0
- JWT 4.4.0

**AI集成**
- DeepSeek API
- 结构化提示词工程

**数据库**
- MySQL 8.0
- Redis 6.2 (缓存)

**工具链**
- Lombok 1.18.30

- Jackson 2.15.3

- PageHelper 5.3.2

  

---

## 快速开始

### 环境要求
1. JDK 17+
2. MySQL 8.0+
3. Maven 3.6+

### 安装步骤
1. 克隆仓库
```bash
git clone https://github.com/yourrepo/food-mgmt-system.git
```

2. 数据库初始化
```sql
CREATE DATABASE food_db;
USE food_db;
source init_schema.sql  # 包含在/sql目录
```

3. 配置修改
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/food_db
    username: root
    password: yourpassword

ai:
  deepseek:
    api-key: your_api_key_here
```

4. 启动应用
```bash
mvn spring-boot:run
```

---

## API文档（部分）

### 食物记录接口
`POST /api/food/record`
```json
{
  "message": "吃了200g牛肉和3个鸡蛋",
  "provider": "deepseek"
}
```

### 菜谱管理接口
`POST /api/recipes/generate`
```javascript
// 响应示例
{
  "ingredients": [
    {"name": "牛肉", "quantity": "200g"},
    {"name": "鸡蛋", "quantity": "3个"}
  ],
  "steps": ["1. 切牛肉...", "2. 热油煎制..."],
  "calories": "450大卡"
}
```

### 已完成相关接口
| 方法   | 端点                             |                功能描述 |
| ------ | :------------------------------- | ----------------------: |
| POST   | /user/register                   |                用户注册 |
| POST   | /user/login                      |                用户登录 |
| PUT    | /user/update                     |                信息更新 |
| PATCH  | /user/updateAvatar               |                头像更新 |
| PATCH  | /user/updatePwd                  |                更新密码 |
| DELETE | /favorites/remove                |                移除收藏 |
| POST   | /favorites/add                   |                添加收藏 |
| GET    | /favorites/list                  |        分页查询收藏列表 |
| POST   | /ai/chat                         |                 GPT服务 |
| GET    | /foods/foodRecordList            |        获取用户食物记录 |
| GET    | /foods/foodEatRecordList         |  获取用户吃完的食物记录 |
| GET    | /foods/foodNormalRecordList      |  获取用户正常的食物记录 |
| GET    | /foods/foodThrowRecordList       |  获取用户浪费的食物记录 |
| POST   | /foods/addRecord                 |        增添一条食物记录 |
| PATCH  | /foods/delRecord                 |        删除一条食物记录 |
| GET    | /foods/getTotalPriceOfThrownFood | 获取用户浪费foods总价格 |
| PATCH  | /foods/getFoodRecordBytype       |    根据分类获取食物记录 |
| GET    | /foods/expiringFoods             |      获取临期产品 7Days |
| PATCH  | /foods/search                    |        模糊查询食物记录 |
| POST   | /foods/eat                       |                食物消耗 |
| POST   | /foods/consumeByAi               |    自然语言处理食物消耗 |
| POST   | /foods/recordByAi                |    自然语言存取食物记录 |
| POST   | /recipe/generate                 |        根据需求生成菜谱 |
| GET    | /recipe/list                     |            分页查询菜谱 |
| ...    | ...                              |                     ... |

## 数据库设计（部分）
**ER Diagram**

```
用户表(user) ──┐
              │ 1:n
食物记录表(food_record) ── n:1 ──菜谱表(recipes)
              │
收藏表(recipe_favorites) ── n:1 ──┘
```

**关键表结构**

```sql
CREATE TABLE recipe_favorites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  recipe_id BIGINT NOT NULL,
  favorite_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY (user_id, recipe_id)
);
```

---

## 开发日志

### 开发日志 - 2024年3月3日

#### 腾讯云COS文件上传功能开发

- 实现基于STS临时密钥的前端直传方案
  - https://cloud.tencent.com/document/product/436/14048
- 修复`InvalidRequest`错误（400状态码）
- 优化权限策略配置，遵循最小权限原则
- 增加文件上传异常处理机制

------

#### **关键问题与解决方案**

|        问题描述         |                      根本原因                       |                           解决方案                           |
| :---------------------: | :-------------------------------------------------: | :----------------------------------------------------------: |
| `secretId is null` 错误 |                   配置未正确注入                    |            使用`@Value`动态注入 + Spring Bean管理            |
|  `ClassCastException`   |                   分页插件未生效                    |                  改用`PageInfo`封装分页结果                  |
| `InvalidRequest (400)`  | 上传流类型（InputStream）需使用TransferManager 实例 |            创建 TransferManager实例，完成相关方法            |
|    文件路径权限不足     |                   资源路径硬编码                    | 动态拼接资源路径：`qcs::cos:{region}:uid/{appid}:{bucket}/user_{id}/*` |
|      内存溢出警告       |               未设置`Content-Length`                |         显式设置`objectMetadata.setContentLength()`          |

------

#### **配置变更记录**

```yaml
# application.yml
tencent:
  oss:
    SecretId: AKID******  # 主账号/子账号密钥
    SecretKey: Rgi*******  # 需妥善保管
    appid: 12515xxxx      # 腾讯云账号APPID
    bucket: icebox-foodai-12xxxx  # 存储桶名称
    region: ap-chongqing    # 地域标识（全小写）
    URL: https://ixxxx-xxxxx.cos.ap-chongqing.myqcloud.com  # 完整Endpoint
```

------

#### **核心代码优化**

1. **STS临时密钥生成**

   java

   ```java
   // 动态构建资源路径
   //通过CosStsClient动态生成包含精细权限的临时密钥
   //采用策略语法生成器动态构建Resource路径，实现租户隔离（user_${userId}目录）
   statement.addResources(new String[]{
       "qcs::cos:" + REGION + ":uid/" + APPID + ":" + BUCKET_NAME + "/user_*/*"
   });
   ```

2. **安全上传流程**

   java

   ```java
   // 显式设置流长度
   ObjectMetadata metadata = new ObjectMetadata();
   metadata.setContentLength(file.getSize());
   ```

3. **TransferManager使用**

   java

   ```java
   // 创建高并发上传客户端
   TransferManager createTransferManager(Map<String,String>map) {
       // 创建一个 COSClient 实例，这是访问 COS 服务的基础实例。
       // 详细代码参见https://cloud.tencent.com/document/product/436/65935
       COSClient cosClient = createCOSClient(map.get("tmpSecretId"),map.get("tmpSecretKey"),map.get("sessionToken"));
   
       // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
       // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
       ExecutorService threadPool = Executors.newFixedThreadPool(32);
   
       // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
       TransferManager transferManager = new TransferManager(cosClient, threadPool);
   
       return transferManager;
   }
   ```

------

#### **待办事项**

- 完善头像上传，食品图像上传功能

------

#### **注意事项**

1. **密钥安全**

   - 禁止将`SecretId/SecretKey`提交到代码仓库
   - 生产环境建议使用子账号密钥

2. **地域一致性**

   - Bucket名称、Endpoint、`region`配置需完全匹配

3. **大文件处理**

   - > 20MB文件需使用分块上传接口

完整代码参见：TencentOssUtils.java



## 问题

1. ### **允许用户添加同名食品**

   **适用场景**：同一用户需要记录多个同名但不同批次、不同购买时间的食品（如多次购买牛奶）。

   ```sql
   -- 删除旧索引
   DROP INDEX idx_user_food ON food_record;
   
   -- 创建新索引（允许同名但不同购买日期的记录）
   CREATE UNIQUE INDEX idx_user_food_date 
   ON food_record(user_id, name, purchase_date);
   ```

   **更新插入逻辑**:确保每次插入时 `purchase_date` 不同：

   ```java
   // 在Java代码中设置不同的购买日期
   foodRecord.setPurchaseDate(new Date()); // 使用实际购买时间而非当前时间
   // 提示词优化确保参考日期计算正确
   String old_prompt = config.getPrompt();
   String head_date ="日期:" + String.valueOf(LocalDate.now()) ;
   String new_prompt = head_date + old_prompt;
   config.setPrompt(new_prompt);
   ```
------
2. ### **在使用AI处理食品记录时，遇到唯一索引冲突导致整个流程中断的问题**

   1. #### **问题记录：**

      - 根据错误日志，当插入“猪肉”记录时，触发了唯一键冲突，因为数据库里已经存在相同的用户ID、食品名称和购买日期。这导致整个Flux流中断，后续的记录无法处理。

   2. #### **问题分析：**

      - `validateAndSave`方法中，使用了`flatMap`来处理每个DTO，但如果有异常抛出，整个流就会停止。这是因为在响应式编程中，默认情况下，一旦出现错误，流会终止。
      
   3. #### **问题思考：**
      1. 需要修改校验和保存逻辑，确保每个记录的处理都是独立的，即使一个失败，其他记录仍能继续。这可以通过为每个记录创建一个独立的事务，并在错误时继续处理后续记录来实现。
      2. 考虑如何处理唯一性校验。当前代码在插入前没有检查是否存在重复记录，而是依赖数据库抛出异常。但这种方式会导致流中断。应该在插入前手动检查是否存在重复，避免触发数据库异常。
      3. 将校验和保存逻辑封装在独立的Mono中，使用`onErrorResume`来捕获异常并返回错误信息，而不是让异常传播到整个流。
      4. 调整事务管理，确保每个记录的插入操作在独立的事务中进行，这样即使一个失败，不会影响其他记录的提交。需要为每个插入操作使用`Propagation.REQUIRES_NEW`的事务传播机制。
      5. 测试修改后的代码，确保当某条记录出现唯一键冲突时，其他记录仍能成功插入，并且错误信息能正确返回给用户，而不是导致整个流程中断。

   4. #### **问题解决：**

      > 通过 **错误隔离 + 事务独立提交** 实现单条失败不影响整体流程。

      1. **修改校验保存逻辑（独立错误处理）**

         ```Java
         private Flux<String> validateAndSave(List<FoodRecordDTO> dtos, Integer userId) {
             return Flux.fromIterable(dtos)
                 .flatMap(dto -> 
                     Mono.fromCallable(() -> {
                         // 参数校验逻辑
                         List<String> errors = validateDTO(dto);
                         if (!errors.isEmpty()) {
                             throw new ValidationException(String.join(", ", errors));
                         }
         
                         // 唯一性校验（关键改进点）
                         boolean exists = foodsMapper.existsByUserAndNameAndDate(
                             userId, 
                             dto.getName(), 
                             dto.getPurchaseDate()
                         );
                         if (exists) {
                             throw new DuplicateKeyException("重复记录: " + dto.getName());
                         }
                         // 转换并保存
          				.......
         }
         ```

      2. **唯一性校验Mapper方法**

         ```sql
         @Select("SELECT COUNT(*) FROM food_record " +
                 "WHERE user_id = #{userId} " +
                 "AND name = #{name} " +
                 "AND purchase_date = #{purchaseDate}")
         boolean existsByUserAndNameAndDate(
             @Param("userId") Integer userId,
             @Param("name") String name,
             @Param("purchaseDate") LocalDate purchaseDate
         );
         ```

      3. **.调整事务传播机制（关键）**

         ```java
         @Service
         public class FoodsRecordServiceImpl {
         
             @Transactional(propagation = Propagation.REQUIRES_NEW) // 每个保存操作独立事务
             public void addRecord(FoodRecord record) {
                 foodsMapper.addRecord(record);
             }
         }
         ```

   5. #### 优化效果

      |          场景          |       原逻辑        |           新逻辑           |
      | :--------------------: | :-----------------: | :------------------------: |
      |    某条记录校验失败    |    整个流程终止     | 仅该记录报错，其他继续处理 |
      | 唯一键冲突（重复记录） | 抛出SQL异常中断流程 |   提前拦截并返回友好提示   |
      | 系统异常（如网络抖动） |       流终止        |  单条标记失败，不影响其他  |

   6. #### **验证测试**

      ```text
      //输入
      名称:猪肉 (已存在)
      名称:牛肉 (新记录)
      名称:豆腐 (日期格式错误)
      //输出
      添加失败[猪肉]: 重复记录: 猪肉
      成功添加记录: 牛肉
      添加失败[豆腐]: 保质期不能早于购买日期
      ```

3. ### **腾讯云COS文件上传功能开发**

   1. #### 腾讯云COS文件上传功能开发

      - 实现基于STS临时密钥的前端直传方案
      - 修复`InvalidRequest`错误（400状态码）
      - 优化权限策略配置，遵循最小权限原则
      - 增加文件上传异常处理机制

   2. #### **关键问题与解决方案**

      |        问题描述         |                      根本原因                       |                           解决方案                           |
      | :---------------------: | :-------------------------------------------------: | :----------------------------------------------------------: |
      | `secretId is null` 错误 |                   配置未正确注入                    |            使用`@Value`动态注入 + Spring Bean管理            |
      |  `ClassCastException`   |                   分页插件未生效                    |                  改用`PageInfo`封装分页结果                  |
      | `InvalidRequest (400)`  | 上传流类型（InputStream）需使用TransferManager 实例 |            创建 TransferManager实例，完成相关方法            |
      |    文件路径权限不足     |                   资源路径硬编码                    | 动态拼接资源路径：`qcs::cos:{region}:uid/{appid}:{bucket}/user_{id}/*` |
      |      内存溢出警告       |               未设置`Content-Length`                |         显式设置`objectMetadata.setContentLength()`          |

4. 使用PageHelper分页参数：

   ```java
   PageHelper.startPage(pageNum, pageSize);
   ```

5. **数据安全**  
   敏感操作需通过JWT认证



## 注意事项

1. **AI响应格式**  
   必须使用结构化提示词确保返回有效JSON

2. **分页查询**  
   使用PageHelper分页参数：
   ```java
   PageHelper.startPage(pageNum, pageSize);
   ```

3. **数据安全**  
   敏感操作需通过JWT认证

4. **类型注解对照表**：

   |    注解     |        适用类型         |                 校验规则                 |
   | :---------: | :---------------------: | :--------------------------------------: |
   | `@NotNull`  |      所有引用类型       |             字段不为 `null`              |
   | `@NotEmpty` | String, Collection, Map | 非 `null` 且内容非空（如字符串长度 > 0） |
   | `@NotBlank` |         String          |        非 `null` 且非全空格字符串        |


---

## 贡献指南
欢迎提交PR！请遵循以下流程：
1. Fork仓库
2. 创建特性分支 (`git checkout -b feature`)
3. 提交修改 (`git commit -m 'Add feature'`)
4. 推送分支 (`git push origin feature`)
5. 创建Pull Request

---

## 许可证
[MIT License](LICENSE)