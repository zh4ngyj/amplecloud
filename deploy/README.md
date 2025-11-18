# 部署指引

该目录包含基于 Docker 的一键部署资产，可将 `eureka-server`、`api-gateway`、`order-service`、`product-service`、OTEL Collector 以及 Jaeger 一次性构建并启动。

## 目录结构

| 路径 | 说明 |
| --- | --- |
| `docker/` | `Dockerfile.service` 为通用镜像模板，通过 `SERVICE`/`VERSION` Build Args 构建任意微服务，且已预置 OTEL Java Agent |
| `docker-compose.yml` | 统一编排全部容器（含 Jaeger）的 Compose 文件 |
| `otel-collector-config.yaml` | 轻量级 OTEL Collector 配置，默认同时导出到 Jaeger 与日志 |
| `deploy.sh` | Bash 脚本，负责 `mvn clean package` + `docker compose up --build -d` |
| `dev/`、`prod/` | 预留环境配置目录，可根据需要扩展 |

## 前置要求

- Docker 24+，并启用 Docker Compose（插件 v2 或 `docker-compose` 命令均可）
- Maven 3.6+ 与 JDK 21（用于构建可执行 JAR）
- 端口未被占用：`8080/8081/8082/8761/4317/4318/16686/14268/14250`
- 终端环境支持 Bash（macOS / Linux / WSL / Git Bash）。Windows 原生 PowerShell 可直接执行 Compose 命令，脚本需在 Bash 环境运行。

## 快速开始

1. **授予脚本执行权限（若在类 Unix 环境）**
   ```bash
   chmod +x deploy/deploy.sh
   ```

2. **一键构建并启动（可选指定版本号，默认 `0.1`）**
   ```bash
   ./deploy/deploy.sh 0.1
   ```

   脚本会：
   - 运行 `mvn clean package -DskipTests`
   - 构建各服务镜像（复用 `deploy/docker/Dockerfile.service` 模板）
   - 用 `docker compose` 启动所有容器（包含 Jaeger 与 OTEL Collector）

3. **验证**
   - Eureka 控制台：`http://localhost:8761`
   - API Gateway：`http://localhost:8080`
   - Swagger 聚合：`http://localhost:8080/swagger-ui.html`
   - Jaeger UI：`http://localhost:16686`
   - 调试 Trace：对 `http://localhost:8080/api/products` 发起请求可以在响应头看到 `X-Trace-Id`，其值与 Jaeger 中的 Trace ID 一致。

## 手动运行（无需脚本）

```bash
mvn clean package -DskipTests
SERVICE_VERSION=0.1 docker compose -f deploy/docker-compose.yml up --build -d
```

若使用 Docker Compose v1（命令为 `docker-compose`），将上述命令替换为 `SERVICE_VERSION=0.1 docker-compose -f ...` 即可。

## 常见操作

| 操作 | 命令 |
| --- | --- |
| 查看容器状态 | `docker compose -f deploy/docker-compose.yml ps` |
| 查看网关日志 | `docker compose -f deploy/docker-compose.yml logs -f api-gateway` |
| 查看 Jaeger Collector 日志 | `docker compose -f deploy/docker-compose.yml logs -f jaeger` |
| 停止并清理 | `docker compose -f deploy/docker-compose.yml down` |
| 只重建某服务 | `docker compose -f deploy/docker-compose.yml up --build -d product-service` |

## 环境变量与版本

- `SERVICE_VERSION`：控制 Compose 构建时使用的 JAR 版本与镜像 tag，需与 `pom.xml` 中 `<version>` 保持一致。
- `OTEL_EXPORTER_OTLP_ENDPOINT`、`OTEL_ENVIRONMENT` 等变量在 Dockerfile 中已有默认值，必要时可通过 Compose `environment` 或 `.env` 覆盖。
- `OTEL_EXPORTER_OTLP_PROTOCOL` 默认为 `grpc`，如果 Collector 仅开放 HTTP/Protobuf（4318），请改为 `http/protobuf` 并同时调整 `OTEL_EXPORTER_OTLP_ENDPOINT`。
- OTEL Collector 通过 `otlphttp` exporter 将 traces 转发到 Jaeger（`http://jaeger:4318`），若要对接其他后端，只需在 `deploy/otel-collector-config.yaml` 中替换 exporter 即可。
- 若需要推送镜像到私有仓库，可在 Compose 的 `image` 字段中修改组织名称或仓库地址。

## OTEL Collector

默认配置仅将 traces/metrics/logs 打印到 Collector 日志。如需转发至可观测性平台，可修改 `deploy/otel-collector-config.yaml` 中的 `exporters` 和 `service.pipelines`。

## 常见问题

1. **脚本提示找不到 Maven**：请确认 `mvn` 已加入 `PATH`，或在执行 `deploy.sh` 前手动构建并跳过脚本。
2. **Windows 无法执行 `.sh` 文件**：使用 WSL、Git Bash 或直接参考“手动运行”命令。
3. **端口被占用**：停止同端口的本地服务，或在 `docker-compose.yml` 中修改 `ports` 映射。
4. **镜像构建失败**：确保 `mvn clean package` 生成了 `target/<module>-<version>.jar`，并且通过 Compose 传入的 `SERVICE`/`SERVICE_VERSION` 与 `deploy/docker/Dockerfile.service` 中的 `ARG` 设置匹配。
5. **OTEL endpoint 变量未生效**：镜像入口脚本 `deploy/docker/entrypoint.sh` 会在容器启动时读取 `OTEL_EXPORTER_OTLP_ENDPOINT` 等环境变量，若自定义 Collector 地址，记得在 Compose 的 `environment` 区块或 `.env` 中覆盖，而不是直接修改 Dockerfile。
