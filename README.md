# Spring Cloud Netflix Demo

该示例演示了基于 Spring Boot 3.2.x 和 Spring Cloud 2023.0.x 的微服务组合，涵盖以下特性：

- **Eureka** 注册中心
- **Spring Cloud Gateway** API 网关
- **Resilience4j Circuit Breaker** 熔断与降级
- **Feign** 声明式服务发现调用
- **Springdoc OpenAPI** 生成 Swagger UI 文档

## 模块结构

| 模块 | 说明 | 端口 |
| ---- | ---- | ---- |
| `eureka-server` | 注册中心，所有服务向其注册 | `8761` |
| `product-service` | 商品目录服务，提供产品查询接口 | `8081` |
| `order-service` | 订单服务，通过 Feign + Resilience4j 调用商品服务并支持降级 | `8082` |
| `api-gateway` | 基于 Spring Cloud Gateway 的统一入口，聚合文档并提供断路器 | `8080` |

## 运行要求

- JDK 21+
- Maven 3.6+

## 快速开始

1. **编译所有模块**
   ```powershell
   mvn clean package
   ```

2. **按顺序启动服务**（建议新开 4 个终端窗口）：

   ```powershell
   # 终端1：启动 Eureka 注册中心
   mvn -pl eureka-server spring-boot:run

   # 终端2：启动商品服务
   mvn -pl product-service spring-boot:run

   # 终端3：启动订单服务
   mvn -pl order-service spring-boot:run

   # 终端4：启动 Spring Cloud Gateway
   mvn -pl api-gateway spring-boot:run
   ```

3. **验证服务注册**
   - 打开浏览器访问 [http://localhost:8761](http://localhost:8761) 查看注册的实例。

4. **通过网关访问 API**
   - 商品列表：`GET http://localhost:8080/api/products`
   - 下单示例：`GET http://localhost:8080/api/orders/p-100`

5. **查看 OpenAPI / Swagger UI**
   - 商品服务：`http://localhost:8081/swagger-ui.html`
   - 订单服务：`http://localhost:8082/swagger-ui.html`
   - 网关聚合：`http://localhost:8080/swagger-ui.html`

   若后端服务不可用，可观察 Resilience4j 的降级响应（例如停掉 `product-service` 后再访问 `/api/orders/p-100`）。

## 配置亮点

- `order-service` 启用 `feign.circuitbreaker.enabled=true`，并通过 `ProductClientFallback` 提供降级响应。
- `api-gateway` 配置全局 `CircuitBreaker` 过滤器，当目标服务不可用时转发至 `/fallback/{serviceId}` 返回统一 JSON。
- `api-gateway` 会在每个响应中追加 `X-Trace-Id` 头部，内容为当前 OpenTelemetry Trace ID，方便调用方将结果与 Jaeger 链路关联。
- 所有服务均注册至 Eureka，并开放 Actuator 监控端点以便排查。
- 通过 Springdoc OpenAPI 自动生成接口文档，便于调试与演示。
- 各服务新增 `logback-spring.xml`，日志模式包含 `traceId/spanId`，便于后续接入 ELK、Jaeger 等观测平台。

## 如何在代码里打印日志

Spring Boot 默认集成 SLF4J + Logback，本项目在每个模块的 `logback-spring.xml` 中已经定义了统一模式：

```text
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level ${appName:-app} - traceId=%X{trace_id:-} spanId=%X{span_id:-} %logger{36} - %msg%n
```

因此只需在代码里使用 `Logger` 打印日志，就能看到随 OpenTelemetry 传入的 trace/span Id。示例（已在 `product-service` 与 `order-service` 中生效）：

```java
private static final Logger log = LoggerFactory.getLogger(ProductController.class);

@GetMapping
public List<Product> findAll() {
   log.info("Listing all products");
   return productCatalog.findAll();
}
```

若已在 `common-dependencies` 引入 Lombok，也可以改用 `@Slf4j` 以减少模板代码；无论哪种方式，日志内容都会自动出现在容器的 STDOUT，且同一个请求会共享同个 `traceId`，方便在 Jaeger 中交叉排查。此外，经由 API Gateway 的请求还会额外返回 `X-Trace-Id` 头部，便于前端或调用方保存日志索引。

## 后续可扩展方向

- **保护系统，防止高流量使系统崩溃**

- **提高数据库并发能力**

- **消息削峰、解耦**

- **接入AI能力**
    - 专用模型：计算机视觉CV、语音处理、决策与分类
    - 生成式模型：大语言LLM、多模态LMM、文成图、视频模型、物理世界模型
    - 模型应用：ASR, TTS, OCR, RAG

- **集成 Spring Cloud Config 统一配置中心**
   - 新增 `config-server` 模块，引入 `spring-cloud-config-server` 并启用 `@EnableConfigServer`。
   - 在 Git 仓库或文件系统中维护共享配置，如 `product-service.yml`、`order-service.yml` 等。
   - 客户端（现有服务）加入 `spring-cloud-starter-config`，在 `bootstrap.yml` 配置 `spring.cloud.config.uri` 并启用 `failFast`/`retry`。
   - 可选添加消息总线（如 RabbitMQ）以支持 `POST /actuator/busrefresh` 热刷新。

- **接入 Micrometer + Prometheus/Grafana 监控链路与熔断指标**
   - 各服务加入 `micrometer-registry-prometheus`，并在 Actuator 中暴露 `/actuator/prometheus`。
   - 利用 Resilience4j 提供的 Micrometer 指标观测断路器状态、重试次数等。
   - 使用 Grafana 仪表盘直观展示请求成功率、失败率以及延迟趋势。

- **日志中心**
