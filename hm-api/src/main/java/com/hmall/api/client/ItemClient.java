package com.hmall.api.client;

import com.hmall.api.client.fallback.ItemClientFallbackFactory;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
//FeignClient注解，声明调用的服务名，并指定fallbackFactory为ItemClientFallbackFactory.class,表示使用ItemClientFallbackFactory.class类创建fallback对象。
@FeignClient(value = "item-service", fallbackFactory = ItemClientFallbackFactory.class) // 调用item-service服务
public interface ItemClient {
    // 查询商品详情
    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids")Collection<Long> ids);

    // 扣减库存
    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    // 回滚库存
    @PutMapping("/items/stock/restore")
    void restoreStock(Long orderId);

    /**
     * 根据ID查询商品详情
     * @param id
     * @return
     */
    @GetMapping("/items/{id}")
    ItemDTO queryItemById(@PathVariable("id") Long id);
}
