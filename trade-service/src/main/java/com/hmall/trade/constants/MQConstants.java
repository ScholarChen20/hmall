package com.hmall.trade.constants;

public interface MQConstants {
    String DELAY_EXCHANGE_NAME = "trade.delay.direct"; // 延迟队列
    String DELAY_ORDER_QUEUE_NAME = "trade.delay.order.queue"; // 延迟队列名
    String DELAY_ORDER_KEY = "delay.order.query"; // 延迟队列路由键
}
