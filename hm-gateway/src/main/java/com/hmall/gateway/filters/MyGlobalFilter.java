package com.hmall.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {
    /**
     * 全局过滤器，作用范围是所有路由，声明后自动生效
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 模拟登录校验逻辑
        ServerHttpRequest request = exchange.getRequest(); // 获取request对象
        HttpHeaders headers = request.getHeaders(); // 获取headers对象
        System.out.println("headers = " + headers);
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 过滤器的执行顺序，值越小优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
