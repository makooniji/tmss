# Game Manager - 游戏管理系统

<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-blue.svg" alt="Spring Boot">
 <img src="https://img.shields.io/badge/JDK-17/21-blue.svg" alt="JDK">
 <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License">
</p>

## 📖 项目介绍

**Game Manager** 是一个基于 Spring Boot 3.5.9 + JDK 17/21 构建的现代化游戏管理系统。本系统采用模块化设计，支持多游戏平台接入、玩家管理、交易记录、数据统计等完整的游戏管理功能。

### 🎯 核心特性

- 🎮 **多平台游戏接入**：支持接入多个游戏供应商（如 JLS 平台），灵活的适配器设计
- 👥 **完整的玩家管理**：玩家注册、登录、账户余额管理、等级体系
- 💰 **交易记录管理**：下注、结算、退款、调整等完整交易流程
- 📊 **数据分析统计**：租户数据统计、游戏数据分析、收入报表
- 🔐 **企业级安全**：支持 Spring Security、多租户隔离、签名验证
- 🔄 **微服务架构**：网关路由、服务治理、分布式事务
- ☁️ **云原生支持**：Docker 容器化、Kubernetes 编排就绪

---

## 📁 项目结构

```
game-manager/
├── gm-dependencies/              # Maven 依赖版本管理
├── gm-framework/                 # 框架核心包
│   ├── yudao-common/             # 通用工具包
│   ├── yudao-spring-boot-starter-* # 各种 Spring Boot Starter
│   └── ...
├── gm-server/                    # 应用服务端 (主服务)
├── gm-gateway/                   # API 网关
├── gm-module-system/             # 系统管理模块（用户、租户、权限等）
├── gm-module-infra/              # 基础设施模块（文件、定时任务等）
├── gm-module-member/             # 玩家中心模块
├── gm-module-game/               # 游戏核心模块 ⭐
│   ├── src/main/java/.../adapter/game/
│   │   ├── base/                 # 游戏适配器基类
│   │   ├── jls/                  # JLS 平台适配器实现
│   │   └── ...
│   ├── src/main/java/.../model/  # 数据模型
│   └── ...
├── script/                       # 部署脚本、Docker 配置、数据库脚本
│   ├── docker/                   # Docker 配置
│   ├── shell/                    # Shell 脚本
│   └── jenkins/                  # CI/CD 配置
├── sql/                          # 数据库脚本
│   ├── mysql/                    # MySQL 脚本
│   ├── postgresql/               # PostgreSQL 脚本
│   └── ...（其他数据库）
└── pom.xml                       # 项目主 POM 文件

```

### 📦 核心模块说明

| 模块 | 说明 | 核心功能 |
|------|------|---------|
| **gm-module-game** | 游戏核心模块 | 游戏适配、下注管理、结算处理 |
| **gm-module-system** | 系统管理模块 | 用户管理、租户管理、权限控制 |
| **gm-module-member** | 玩家中心模块 | 玩家账户、积分、等级体系 |
| **gm-module-infra** | 基础设施模块 | 文件服务、定时任务、代码生成 |
| **gm-server** | 主应用服务 | 业务服务启动类 |
| **gm-gateway** | API 网关 | 请求路由 |

---

## 🔧 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.5.9 | 应用开发框架 |
| **MyBatis Plus** | 3.5.12 | ORM 框架 |
| **Redis** | 5.0+ | 缓存、会话管理 |
| **Spring Security** | 6.5.2 | 认证授权框架 |
| **MySQL** | 5.7/8.0+ | 关系型数据库 |
| **Docker** | - | 容器化部署 |

### 开发工具与库

| 工具/库 | 版本 | 用途 |
|--------|------|------|
| **JDK** | 17/21 | Java 开发环境 |
| **Maven** | 3.8+ | 项目构建工具 |
| **Lombok** | 1.18.42 | 代码简化 |
| **Hutool** | - | Java 工具库 |
| **FastJSON** | - | JSON 处理 |
| **Swagger/Springdoc** | 2.8.9 | API 文档 |

---

## 🚀 快速开始

### 📋 前置条件

- **JDK 17** 或更高版本（推荐 JDK 17/21）
- **Maven 3.8+**
- **MySQL 5.7** 或更高版本
- **Redis 5.0** 或更高版本
- **Docker**（可选，用于部署）

### 📝 环境变量配置

在 `gm-server/src/main/resources/application.yml` 或 `application-dev.yml` 中配置：

```yaml
spring:
  application:
    name: gm-server
  datasource:
    url: jdbc:mysql://localhost:3306/game_manager?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379
    password: 
  
server:
  port: 9095
```

### 🔨 编译构建

```bash
# 1. 克隆项目
git clone <repository-url>
cd game-manager

# 2. 使用 Maven 编译
mvn clean package -DskipTests

# 3. 编译指定模块
mvn clean package -f gm-server/pom.xml

# 4. 跳过测试（加快编译）
mvn clean install -DskipTests
```

### ▶️ 启动应用

#### 方式一：直接运行 JAR

```bash
cd gm-server
mvn spring-boot:run
```

#### 方式二：编译后运行

```bash
cd gm-server/target
java -jar gm-server-*.jar
```

#### 方式三：Docker 运行

```bash
# 构建镜像
docker build -t game-manager:latest -f gm-server/Dockerfile .

# 运行容器
docker run -d \
  --name game-manager \
  -p 9095:9095 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/game_manager \
  -e SPRING_REDIS_HOST=redis \
  game-manager:latest
```

### 📊 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p < sql/mysql/init.sql

# 2. 导入表结构
mysql -u root -p game_manager < sql/mysql/tables.sql

# 3. 导入初始数据
mysql -u root -p game_manager < sql/mysql/data.sql
```

### ✅ 验证安装

应用启动成功后，访问以下地址：

- **应用健康检查**：http://localhost:9095/actuator/health
- **API 文档**：http://localhost:9095/swagger-ui.html 或 http://localhost:9095/doc.html
- **应用主页**：http://localhost:9095/

---

## 📚 核心功能介绍

### 🎮 游戏管理

系统采用**适配器模式**设计，支持多游戏平台无缝接入。

#### 游戏适配器架构

```
PlatformRequest (请求)
       ↓
BaseAdapter (基类)
       ↓
JLsAdapter / XXXAdapter (具体实现)
       ↓
PlatformResponse (响应)
```

#### 关键类说明

- **`BaseAdapter`**：游戏适配器基类，定义接口规范
- **`JLsAdapter`**：JLS 平台适配器实现示例
- **`PlatformRequest`**：平台请求数据结构
- **`PlatformResponse`**：平台响应数据结构
- **`SportContext`**：游戏上下文信息

#### 支持的游戏操作

| 操作 | 说明 | 对应方法 |
|------|------|---------|
| **balance** | 查询余额 | `doForward()` |
| **bet** | 下注 | `buildBet()` |
| **bet_result** | 结算 | `buildBetResult()` |
| **rollback** | 退款 | `buildRefund()` |
| **adjustment** | 调整 | `buildAdjustment()` |

### 👥 玩家管理

- 玩家账户创建与管理
- 账户余额实时更新
- 交易历史记录
- 玩家等级与等级体系
- 多租户隔离

### 💳 交易管理

- **下注管理**：记录所有玩家下注
- **结算处理**：自动结算游戏结果
- **退款流程**：支持异常退款
- **账户调整**：手动调整玩家账户
- **交易审计**：完整的交易日志

### 📈 数据统计

- 租户每日数据统计
- 游戏收入报表
- 玩家行为分析
- 实时监控面板

### 🔐 安全特性

- **签名验证**：所有请求需要签名验证（SignUtils）
- **多租户隔离**：不同租户数据完全隔离
- **权限控制**：基于 Spring Security 的权限管理
- **数据加密**：敏感数据加密存储

---

## 🔌 API 文档

### 查询余额

```http
POST /api/game/balance
Content-Type: application/json

{
  "action": "balance",
  "username": "user123",
  "currency": "CNY",
  "sign": "xxxxx"
}
```

### 下注请求

```http
POST /api/game/bet
Content-Type: application/json

{
  "action": "bet",
  "username": "user123",
  "betId": "bet_001",
  "betAmount": 100,
  "roundId": "round_001",
  "gameCode": "game001",
  "sign": "xxxxx"
}
```

### 游戏结算

```http
POST /api/game/settle
Content-Type: application/json

{
  "action": "bet_result",
  "betId": "bet_001",
  "resultType": "WIN",
  "winAmount": 200,
  "sign": "xxxxx"
}
```

更多 API 详情请查看 Swagger 文档：`http://localhost:9095/swagger-ui.html`

---

## 🛠️ 常见问题

### Q1：启动时提示数据库连接失败

**A：** 检查以下几点：
- 确认 MySQL 服务已启动
- 检查 `application.yml` 中的数据库配置（URL、用户名、密码）
- 确保数据库已创建，表已初始化
- 检查防火墙是否放行 3306 端口

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/game_manager?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

### Q2：Redis 连接失败

**A：** 
- 确认 Redis 服务已启动（`redis-server`）
- 检查 Redis 配置：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 10000
```

### Q3：签名验证失败

**A：** 确保：
- 使用正确的 `VendorKey` 进行签名
- 签名算法与平台方协议一致
- 检查 `SignUtils.verifySign()` 的实现

### Q4：多租户隔离不生效

**A：** 检查：
- 请求头中是否包含租户标识（通常在 Token 中）
- 配置是否启用了多租户功能
- 数据库查询是否添加了租户条件

### Q5：如何添加新的游戏平台？

**A：** 参考下面的扩展指南。

---

## 🔧 开发指南

### 添加新的游戏平台适配器

假设要添加 "XYZ" 游戏平台：

#### 1️⃣ 创建适配器类

```java
@Service
@Slf4j
public class XyzAdapter extends BaseAdapter {
    
    @Override
    public PlatformResponse doForward(PlatformRequest param, VendorTenantDO vendorTenantDO) {
        // 实现平台特定的业务逻辑
        return PlatformResponse.success();
    }
    
    @Override
    protected SportContext buildContext(PlatformRequest param, VendorTenantDO vendorTenantDO) {
        SportContext ctx = new SportContext();
        // 构建上下文
        return ctx;
    }
    
    // 实现其他抽象方法...
}
```

#### 2️⃣ 注册平台映射

在 `PlatformCodeEnum` 中添加：

```java
public enum PlatformCodeEnum {
    JLS("jls", "JLS Platform"),
    XYZ("xyz", "XYZ Platform"),
    // ...
}
```

#### 3️⃣ 配置平台信息

在数据库 `game_vendor` 表中添加平台记录。

#### 4️⃣ 实现平台特定逻辑

根据 XYZ 平台的 API 文档，实现具体的请求/响应处理。

### 数据模型扩展

#### 添加新的交易类型

1. 在 `ActionEnum` 中添加新的操作类型
2. 在对应的 Adapter 中实现处理逻辑
3. 更新数据库表结构（if needed）

### 测试

```bash
# 运行单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=JLsAdapterTest

# 跳过测试打包
mvn clean package -DskipTests
```

---

## 📦 部署指南

### Docker 部署

#### 1️⃣ 编写 Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY gm-server/target/gm-server-*.jar app.jar
EXPOSE 9095
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2️⃣ 构建镜像

```bash
docker build -t game-manager:latest .
```

#### 3️⃣ 运行容器

```bash
docker run -d \
  --name game-manager \
  -p 9095:9095 \
  --link mysql:mysql \
  --link redis:redis \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/game_manager \
  -e SPRING_REDIS_HOST=redis \
  game-manager:latest
```

### Docker Compose 部署

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: game_manager
      MYSQL_ROOT_PASSWORD: 123456
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7.0
    ports:
      - "6379:6379"

  game-manager:
    build: .
    ports:
      - "9095:9095"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/game_manager
      SPRING_REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

启动：

```bash
docker-compose up -d
```

### Kubernetes 部署

（待补充 K8s 部署配置）

---

## 📖 文档资源

- 📘 [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- 📗 [MyBatis Plus 文档](https://mp.baomidou.com/)
- 📙 [芋道项目文档](https://doc.iocoder.cn/)
- 📕 [Swagger API 文档](http://localhost:9095/swagger-ui.html)

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 提交流程

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循《阿里巴巴 Java 开发手册》
- 添加适当的代码注释
- 编写单元测试
- 提交前运行 `mvn clean package`

---

## 📄 许可证

本项目采用 **MIT License** 开源协议，个人与企业可 100% 免费使用。

详见 [LICENSE](./LICENSE) 文件。

---

## 👥 致谢

感谢以下项目的灵感与参考：

- [RuoYi-Vue-Pro](https://github.com/YunaiV/ruoyi-vue-pro)
- [Yudao-Cloud](https://github.com/YunaiV/yudao-cloud)
- [Spring Boot Labs](https://github.com/yudaocode/SpringBoot-Labs)

---

## 📞 联系方式

如有任何问题或建议，欢迎联系：

- 📧 Email：support@example.com
- 💬 Issues：提交 GitHub Issues
- 📱 微信：（预留）

---

## 🎯 后续规划

- [ ] 支持更多游戏平台
- [ ] 增强实时监控面板
- [ ] 完善支付系统集成
- [ ] 大屏数据可视化
- [ ] 移动端 App（UniApp）
- [ ] AI 风控系统
- [ ] 国际化支持（i18n）

---

**最后更新：2026-03-30**

**项目版本：2026.01-SNAPSHOT**

⭐️ 如果这个项目对你有帮助，请给个 Star，谢谢！

