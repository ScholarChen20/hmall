package com.hmall.search.listener;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.search.domain.po.ItemDoc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemListener {

    private final ItemClient itemClient;

    @Resource
    private  RestHighLevelClient client;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.change.queue"),
            exchange = @Exchange(name = "es.item.direct"),
            key = "item.change"
    ))
    private void handleItemChange(ItemDoc itemDoc, Message message) throws IOException, IOException {
        //建立连接
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.100.128:9200")
        ));
        //判断当前是什么操作
        String method = message.getMessageProperties().getHeader("method");
        if("add".equals(method)){
            // 3.将ItemDTO转json
            String doc = JSONUtil.toJsonStr(itemDoc);
            // 1.准备Request对象
            IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
            // 2.准备Json文档
            request.source(doc, XContentType.JSON);
            // 3.发送请求
            client.index(request, RequestOptions.DEFAULT);
            //新增
        }else if("update".equals(method)){
            //1.准备request对象
            UpdateRequest request = new UpdateRequest("items",itemDoc.getId());
            //2.准备请求体
            request.doc(JSONUtil.toJsonStr(method));
            //3.发送请求
            try {
                client.update(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //修改
        }else if("delete".equals(method)){
            //删除
            //1.准备request对象
            DeleteRequest request = new DeleteRequest("items").id(itemDoc.getId());
            //2.发送请求
            client.delete(request, RequestOptions.DEFAULT);
        }else {
            log.error("无法匹配操作类型");
        }
        //断开连接
        client.close();
    }
//    private final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
//            HttpHost.create("192.168.100.128:9200")
//    ));
//    public void common(Long id) throws Exception {
//        ItemDTO item = itemClient.queryItemById(id);
//        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
//        //设置更新时间
////        itemDoc.setUpdatedTime(System.currentTimeMillis());
//        //1.准备request
//        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
//        //2.准备参数
//        String jsonStr = JSONUtil.toJsonStr(itemDoc);
//        request.source(jsonStr, XContentType.JSON);
//        //3.发送请求
//        client.index(request, RequestOptions.DEFAULT);
//        System.out.println("我检测到mq给我发送商品新增的消息了，我被触发然后开始存入信息" + jsonStr);
//    }
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "search.item.index.queue", durable = "true"),
//            exchange = @Exchange(value = "search.direct"),
//            key = "item.index"
//    ))
//    public void listenItemIndex(Long id) throws Exception {
//        log.info("接收到商品索引消息，商品id：{}", id);
//        common(id);
//        log.info("新增商品id：{}的索引完成", id);
//    }
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "search.item.status.queue", durable = "true"),
//            exchange = @Exchange(value = "search.direct"),
//            key = "item.updateStatus"
//    ))
//    public void listenItemUpdateStatus(Long id) throws Exception {
//        log.info("商品索引更新开始，商品id：{}", id);
//        ItemDTO itemDTO = itemClient.queryItemById(id);
//        ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
//        String jsonStr = JSONUtil.toJsonStr(itemDoc);
//        //1.准备request
//        UpdateRequest request = new UpdateRequest("items", itemDoc.getId());
//        request.doc(jsonStr, XContentType.JSON);
//        //3.发送请求
//        client.update(request, RequestOptions.DEFAULT);
//        System.out.println("我检测到mq给我发送商品更新状态的消息了，我被触发然后开始更新信息"+jsonStr);
//    }
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "search.item.delete.queue", durable = "true"),
//            exchange = @Exchange(value = "search.direct"),
//            key = "item.delete"
//    ))
//    public void listenItemDelete(Long id) throws Exception {
//        log.info("接收到商品索引删除消息，商品id：{}", id);
//        //1.准备request
//        DeleteRequest request = new DeleteRequest("item", id.toString());
//        //2.发送请求
//        client.delete(request, RequestOptions.DEFAULT);
//        System.out.println("我检测到mq给我发送商品删除的消息了，我被触发然后开始删除信息"+ id);
//    }

}
