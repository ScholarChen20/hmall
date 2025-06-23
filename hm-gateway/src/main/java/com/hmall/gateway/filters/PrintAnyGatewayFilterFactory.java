package com.hmall.gateway.filters;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {
    /**
     * 自定义过滤器工厂，用于打印任意参数。必须GatewayFilterFactory接口的实现类。
     * @param config
     * @return
     */
    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(new GatewayFilter(){
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String a = config.getA();
                String b = config.getB();
                String c = config.getC();
                System.out.println("a = " + a);
                System.out.println("b = " + b);
                System.out.println("c = " + c);
                System.out.println("print any filter running");
                return chain.filter(exchange);
            }
        }, 1);
    }

    /**
     * 配置类，用于接收参数。
     * 必须有默认构造函数。
     */
    @Data
    public static class Config{
        private String a;
        private String b;
        private String c;
    }

    /**
     * 构造函数，用于初始化配置类。将config字节码传递给父类，并初始化配置类。
     */
    public PrintAnyGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * 重写shortcutFieldOrder方法，用于指定参数顺序。
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("a", "b", "c");
    }
}
