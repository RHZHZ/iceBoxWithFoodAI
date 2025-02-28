```markdown
# 智能食物管理系统

基于AI和Spring Boot的智能食物管理系统，提供自然语言处理、菜谱生成、库存管理临期提醒及用户收藏等功能。

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
- [数据库设计](#数据库设计)
- [注意事项](#注意事项)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 功能特性
| 模块         | 功能描述                                                                 |
|--------------|--------------------------------------------------------------------------|
| AI食物识别    | 解析自然语言输入（如"吃了300g全麦面包"）并更新库存                       |
| 智能菜谱生成  | 根据食材生成包含营养成分和步骤的菜谱                                    |
| 用户收藏系统  | 支持菜谱收藏/取消收藏，带分页查询功能                                    |
| 库存管理      | 实时追踪食物库存状态，支持多单位换算                                     |
| ....   | ....                                     |
---

## 技术栈
**后端框架**
- Spring Boot 3.1.5
- MyBatis 3.0.3
- Reactor 3.4.12 (响应式编程)

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

## API文档

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

### 用户收藏接口
| 方法   | 端点                   | 功能描述               |
|--------|------------------------|-----------------------|
| POST   | /favorites/add        | 添加收藏              |
| DELETE | /favorites/remove     | 移除收藏              |
| GET    | /favorites/list       | 分页查询收藏列表      |

---

## 数据库设计
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
....
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
```