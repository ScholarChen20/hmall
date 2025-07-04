package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    /**
     * 创建fallback对象，当调用ItemClient接口的查询方法失败时，返回的对象会调用create方法，并传入调用失败的原因
     * @param cause
     * @return
     */
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！", cause);
                return CollUtils.emptyList();// 返回空列表
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败！", cause);
                throw new RuntimeException(cause);
            }

            //新加的方法
            @Override
            public void restoreStock(Long orderId) {
                log.error("恢复商品库存失败！", cause);
                throw new RuntimeException(cause);
            }

            @Override
            public ItemDTO queryItemById(Long id) {
                log.error("根据id查询出现异常: {}", id);
                return null;// 返回null
            }
        };
    }
}
