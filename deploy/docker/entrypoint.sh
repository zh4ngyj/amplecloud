#!/bin/sh
set -eu

exec java \
  -javaagent:/app/opentelemetry-javaagent.jar \
  -Dotel.service.name="${SERVICE_NAME:-service}" \
  -Dotel.exporter.otlp.endpoint="${OTEL_EXPORTER_OTLP_ENDPOINT:-http://otel-collector:4317}" \
  -Dotel.resource.attributes="deployment.environment=${OTEL_ENVIRONMENT:-dev},service.version=${SERVICE_VERSION:-0.1}" \
  -Dotel.traces.sampler=parentbased_traceidratio \
  -Dotel.traces.sampler.arg=0.1 \
  -jar /app/app.jar
