package com.hmall.api.client.fallback;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
//fallback对象实现shelf接口，并实现接口方法，创建fallback对象，当调用PayClient接口的查询方法失败时，返回fallback对象，不影响业务
@Slf4j
public class PayClientFallback implements FallbackFactory<PayClient> {
    /**
     * 创建fallback对象
     * @param cause
     * @return
     */
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient() {
            @Override
            public PayOrderDTO queryPayOrderByBizOrderNo(Long id) {
                return null;
            } // 返回null，不影响业务
        };
    }
}