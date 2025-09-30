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
- 所有服务均注册至 Eureka，并开放 Actuator 监控端点以便排查。
- 通过 Springdoc OpenAPI 自动生成接口文档，便于调试与演示。

## 后续可扩展方向

- **集成 Spring Cloud Config 统一配置中心**
   - 新增 `config-server` 模块，引入 `spring-cloud-config-server` 并启用 `@EnableConfigServer`。
   - 在 Git 仓库或文件系统中维护共享配置，如 `product-service.yml`、`order-service.yml` 等。
   - 客户端（现有服务）加入 `spring-cloud-starter-config`，在 `bootstrap.yml` 配置 `spring.cloud.config.uri` 并启用 `failFast`/`retry`。
   - 可选添加消息总线（如 RabbitMQ）以支持 `POST /actuator/busrefresh` 热刷新。

- **引入 Spring Cloud Sleuth + Zipkin 做链路追踪**
   - 所有服务添加依赖 `spring-cloud-starter-sleuth` 以及（任选其一）`spring-cloud-sleuth-zipkin` 或 `zipkin-server`。
   - Zipkin Server 可单独起一个模块，或使用官方 Docker 镜像 `openzipkin/zipkin`。
   - 通过 Sleuth 自动注入的 `traceId`、`spanId` 关联跨服务调用，可在 Zipkin UI（默认 `http://localhost:9411`）查看链路拓扑与耗时。

- **接入 Micrometer + Prometheus/Grafana 监控链路与熔断指标**
   - 各服务加入 `micrometer-registry-prometheus`，并在 Actuator 中暴露 `/actuator/prometheus`。
   - 利用 Resilience4j 提供的 Micrometer 指标观测断路器状态、重试次数等。
   - 使用 Grafana 仪表盘直观展示请求成功率、失败率以及延迟趋势。
