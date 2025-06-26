package com.hmall.trade.constants;

public interface MQConstants {
    String DELAY_EXCHANGE_NAME = "trade.delay.direct"; // 延迟队列
    String DELAY_ORDER_QUEUE_NAME = "trade.delay.order.queue"; // 延迟队列名
    String DELAY_ORDER_KEY = "delay.order.query"; // 延迟队列路由键


    /// 消费者确认机制，ack 确认机制(mq从队列中删除消息),
    ///  nack 取消确认机制（再次投递）,
    /// reject 拒绝确认机制（mq删除消息，拒绝消息）
    String CONFIRM_EXCHANGE_NAME = "trade.confirm.direct"; // 确认队列
    String CONFIRM_QUEUE_NAME = "trade.confirm.queue"; // 确认队列名
    String CONFIRM_KEY = "confirm.order.query"; // 确认队列路由键
}
