# Spring Cloud Netflix Demo

该示例演示了基于 Spring Boot 2.3.x 和 Spring Cloud Hoxton.SR12 的经典 Netflix OSS 组件组合，涵盖以下特性：

- **Eureka** 注册中心
- **Zuul** API 网关
- **Hystrix** 熔断与服务降级
- **Feign** 声明式服务发现调用
- **Springdoc OpenAPI** 生成 Swagger UI 文档

## 模块结构

| 模块 | 说明 | 端口 |
| ---- | ---- | ---- |
| `eureka-server` | 注册中心，所有服务向其注册 | `8761` |
| `product-service` | 商品目录服务，提供产品查询接口 | `8081` |
| `order-service` | 订单服务，通过 Feign 调用商品服务并启用 Hystrix 降级 | `8082` |
| `api-gateway` | 基于 Zuul 的统一入口，路由至后端服务并提供全局熔断 | `8080` |

## 运行要求

- JDK 8+
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

   # 终端4：启动 Zuul 网关
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

   若后端服务不可用，可观察 Hystrix 的降级响应（例如停掉 `product-service` 后再访问 `/api/orders/p-100`）。

## 配置亮点

- `order-service` 启用 `feign.hystrix.enabled=true`，并提供 `ProductClientFallback` 作为熔断降级实现。
- `api-gateway` 定义全局 `FallbackProvider`，为所有路由提供统一的 JSON 降级响应。
- 所有服务均注册至 Eureka，并开放 Actuator 监控端点以便排查。
- 通过 Springdoc OpenAPI 自动生成接口文档，便于调试与演示。

## 后续可扩展方向

- 集成 Spring Cloud Config 统一配置中心。
- 引入 Spring Cloud Sleuth + Zipkin 做链路追踪。
- 将 Hystrix Dashboard / Turbine 加入以可视化熔断指标。
