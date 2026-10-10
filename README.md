# Order Service · 订单管理系统 API

一个生产级 REST API 示例：**Spring Boot 3 + RBAC 权限 + 乐观锁防超卖 + 事务 + 单元/集成测试 + Docker 化**。
内置 **H2 内存库演示模式**，克隆即可跑，无需装 MySQL。

---

## 技术栈

| 项 | 版本 / 说明 |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.4 |
| 安全 | Spring Security 6 + JWT (jjwt 0.12.6) + **RBAC** |
| 持久层 | Spring Data JPA (Hibernate 6) + **乐观锁 (@Version)** |
| 缓存 | Spring Cache（demo 用 simple，生产可切 Redis） |
| 监控 | Spring Boot Actuator (health/info) |
| 数据库 | H2（演示）/ MySQL 8（生产） |
| 文档 | springdoc-openapi (Swagger UI) |
| 测试 | JUnit 5 + MockMvc + AssertJ（**10 个测试**） |

## 核心亮点

- ✅ **RBAC 权限模型**：`ROLE_ADMIN` 可管商品，`ROLE_USER` 只能下单；方法级 + 路径级双重控制
- ✅ **乐观锁防超卖**：商品库存带 `@Version`，并发下单冲突返回 `409` 而非超卖
- ✅ **事务一致性**：下单 = 校验 → 扣库存 → 生成快照，任一失败整单回滚
- ✅ **库存回滚**：取消订单自动归还库存
- ✅ **数据快照**：订单项保存下单时的商品名与单价，避免商品改价影响历史订单
- ✅ **统一响应 + 全局异常**：HTTP 状态码与响应体 code 一致（400/401/403/404/409/500），不泄露堆栈
- ✅ **软删除**：商品下架不物理删除，保留历史订单引用
- ✅ **缓存**：商品详情 `@Cacheable`，写操作 `@CacheEvict`
- ✅ **完整测试**：4 个业务单测 + 6 个接口集成测试

## 快速开始（30 秒，无需 MySQL）

```bash
mvn spring-boot:run
```

| 入口 | 地址 |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 控制台 | http://localhost:8080/h2-console |
| 健康检查 | http://localhost:8080/actuator/health |

**演示账号**：

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `user` | `user123` | ROLE_USER |

> demo profile 自动建表 + 播种 5 个商品。

## 跑测试

```bash
mvn test
```

预期输出：

```
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**测试覆盖**：
- 下单扣库存 / 金额计算 / 商品快照
- 库存不足整单拒绝
- 取消订单库存回滚
- 越权取消他人订单被拒
- 公开接口免登录
- 未登录 401 / 普通用户越权 403 / 管理员成功
- 下单主流程端到端
- 参数校验 400

## API 示例

**1) 登录**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user","password":"user123"}'
```

**2) 浏览商品（公开，无需登录）**

```bash
curl -s "http://localhost:8080/api/products?page=1&size=5"
```

**3) 下单**（`$JWT` 为上一步返回的 token）

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $JWT" -H 'Content-Type: application/json' \
  -d '{"items":[{"productId":1,"quantity":2}]}'
```

**4) 管理员新增商品（普通用户访问返回 403）**

```bash
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_JWT" -H 'Content-Type: application/json' \
  -d '{"name":"新品键盘","price":299.00,"stock":10}'
```

## 用 MySQL 跑

```bash
export DB_HOST=127.0.0.1 DB_NAME=order_service DB_USER=root DB_PASSWORD=yourpwd
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Docker 部署

```bash
docker compose up --build
```

启动后：应用 `:8080`，MySQL `:3306`。

## 项目结构

```
src/main/java/com/demo/order/
├── OrderServiceApplication.java
├── common/        # 统一响应 / 分页 / 业务异常 / 全局异常处理
├── config/        # SecurityConfig(RBAC) / OpenApiConfig / DataInitializer
├── security/      # JwtUtil / JwtAuthFilter / UserDetailsServiceImpl
├── entity/        # SysUser / Product(@Version) / Order / OrderItem
├── repository/    # JPA 仓储（条件检索 / EntityGraph 预取）
├── dto/           # 请求/响应对象 + 校验注解
├── service/       # AuthService / ProductService / OrderService（核心事务）
└── controller/    # Auth / Product / Order

src/test/java/com/demo/order/
├── OrderServiceTest.java            # 业务逻辑单测
└── OrderApiIntegrationTest.java     # 接口集成测试（MockMvc）
```

## 生产注意事项

- ⚠️ `app.jwt.secret` 必须通过环境变量 `APP_JWT_SECRET` 覆盖（Base64，≥32 字节）
- ⚠️ 演示账号仅在 `demo` profile 存在，生产请接入真实用户体系
- ⚠️ H2 控制台仅 demo 放开，生产请关闭
- 建议补充：Redis 缓存、接口限流、分布式锁（多实例下单）、操作审计日志、CI/CD

---

**Java 后端 · 10 年经验 · 可接管理系统 / 接口开发 / 系统维护 / AI 集成**
