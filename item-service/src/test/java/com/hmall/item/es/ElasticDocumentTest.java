package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local") // 指定环境，测试类，启动类
public class ElasticDocumentTest {

    private RestHighLevelClient client; // EsClient
    @Autowired
    private IItemService itemService;


    @Test
    void testAddDocument() throws IOException {
        // 1.根据id查询商品数据
        Item item = itemService.getById(100002644680L);
        // 2.转换为文档类型
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 3.将ItemDTO转json
        String doc = JSONUtil.toJsonStr(itemDoc);

        // 1.准备Request对象
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 2.准备Json文档
        request.source(doc, XContentType.JSON);
        // 3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }
    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    void testIndexDoc() throws IOException {
        // 0.准备文档数据
        // 0.1.根据id查询数据库数据
        Item item = itemService.getById(100002644680L);
        // 0.2.把数据库数据转为文档数据
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);

        itemDoc.setPrice(29900);

        // 1.准备Request
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId()); //item.getId().toString()普通类型转为String类型
        // 2.准备请求参数
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        // 3.发送请求
        IndexResponse resp = client.index(request, RequestOptions.DEFAULT);
        System.out.println("resp = " + resp);
    }

    /**
     * 查询文档
     * @throws IOException
     */
    @Test
    void testGetDoc() throws IOException {
        // 1.准备Request
        GetRequest request = new GetRequest("items").id("100002644680");
        // 2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3.解析响应结果
        String json = response.getSourceAsString();
        ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println("doc = " + doc);
    }

    /**
     * 删除文档
     * @throws IOException
     */
    @Test
    void testDeleteDoc() throws IOException {
        // 1.准备Request
        DeleteRequest request = new DeleteRequest("items", "100002644680");
        // 2.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 更新文档
     * @throws IOException
     */
    @Test
    void testUpdateDoc() throws IOException {
        // 1.准备Request
        UpdateRequest request = new UpdateRequest("items", "100002644680");
        // 2.准备请求参数
        request.doc(
                "price", 25600
        );
        // 3.发送请求
        client.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量插入文档
     * @throws IOException
     */
    @Test
    void testBulkDoc() throws IOException {
        int pageNo = 1, pageSize = 500;
        while (true) {
            // 1.准备文档数据
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if(records == null || records.isEmpty()){
                return;
            }

            // 2.准备Request
            BulkRequest request = new BulkRequest();  //blukRequest批量请求
            // 3.准备请求参数
            for (Item item : records) {
                request.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)), XContentType.JSON));
            }
            // 4.发送请求
            client.bulk(request, RequestOptions.DEFAULT);
            // 5.翻页
            pageNo++;
        }
    }

    /**
     * 创建EsClient, 连接到Es集群
     */
    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.100.128:9200")
        ));
    }

    /**
     * 关闭EsClient
     * @throws IOException
     */

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
