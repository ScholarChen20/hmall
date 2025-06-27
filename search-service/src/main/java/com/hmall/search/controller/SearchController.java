package com.hmall.search.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.domain.vo.CategoryAndBrandVo;
import com.hmall.search.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;

    //创建ES客户端,连接ES集群
    private final RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("http://192.168.100.128:9200")
    ));

    private final ItemClient itemClient;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public PageDTO<ItemDoc> list(ItemPageQuery query) {
        return searchService.EsSearch(query);
    }

//    @ApiOperation("搜索商品")
//    @GetMapping("/{id}")
//    public ItemDTO search(@PathVariable("id") Long id) throws IOException {
//        //1.准备Request
//        GetRequest request = new GetRequest("items",id.toString());
//        //2.发送请求
//        GetResponse response= client.get(request, RequestOptions.DEFAULT);
//        //3.解析响应
//        String item = response.getSourceAsString();
//        //4.反序列化为对象
//        ItemDoc itemDoc = JSONUtil.toBean(item, ItemDoc.class);
//        ItemDTO itemDTO = BeanUtil.copyProperties(itemDoc, ItemDTO.class);
//        // 5.返回
//        return itemDTO;
//    }
    @ApiOperation("分类聚合接口")
    @PostMapping("/filters")
    public CategoryAndBrandVo getFilters(@RequestBody ItemPageQuery query) {
        return searchService.getFilters(query);
    }
}
