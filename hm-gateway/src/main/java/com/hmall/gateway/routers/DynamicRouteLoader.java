package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component // 组件，自动装配，自动注入
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;  // 注入nacos配置管理器
    private final RouteDefinitionWriter writer; // 路由定义写入器

    private final String dataId = "gateway-routes.json"; // 配置文件名称
    private final String group = "DEFAULT_GROUP"; // 配置文件分组

    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct   // 项目启动时执行
    public void initRouteConfigListener() throws NacosException {
        // 1.项目启动时，先拉取一次配置，并且添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 2.监听到配置变更，需要去更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        // 3.第一次读取到配置，也需要更新到路由表
        updateConfigInfo(configInfo);
    }

    /**
     * 监听到配置信息变更，更新路由表
     * @param configInfo
     */
    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息：{}", configInfo);
        // 1.解析配置信息，转为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        // 2.删除旧的路由表
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();

        // 3.更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            // 3.1.更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe(); // 保存路由，subscribe()用于订阅，等待保存完成
            // 3.2.记录路由id，便于下一次更新时删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
