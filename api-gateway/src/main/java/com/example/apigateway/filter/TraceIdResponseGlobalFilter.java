package com.example.apigateway.filter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * Adds the current trace id to every HTTP response so callers can correlate payloads with Jaeger spans.
 */
@Component
public class TraceIdResponseGlobalFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            String traceId = currentTraceId();
            if (StringUtils.hasText(traceId)) {
                exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);
            }
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    private String currentTraceId() {
        Span currentSpan = Span.current();
        if (currentSpan == null) {
            return null;
        }
        SpanContext context = currentSpan.getSpanContext();
        if (context == null || !context.isValid()) {
            return null;
        }
        return context.getTraceId();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
