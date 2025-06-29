package com.hmall.search.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.search.domain.po.Item;
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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private final ISearchService itemService;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public PageDTO<ItemDTO> list(ItemPageQuery query) throws IOException {
        // 分页查询
//        Page<Item> result = itemService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        return PageDTO.of(result, ItemDTO.class);
//        return  searchService.EsSearch(query);
        SearchRequest searchRequest = new SearchRequest("items"); // 索引库名称
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery(); // 构造查询条件
        if(StrUtil.isNotBlank(query.getKey())){
            queryBuilder.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if(StrUtil.isNotBlank(query.getBrand())){
            queryBuilder.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        if(StrUtil.isNotBlank(query.getCategory())){
            queryBuilder.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        if(query.getMaxPrice()!=null){
            queryBuilder.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()).lte(query.getMaxPrice()));
        }
        //sort排序
        searchRequest.source().query(queryBuilder)
                .size(query.getPageSize())
                .from(query.from());
        int pageSize = query.getPageSize() == null ? 10 : query.getPageSize(); // 默认10条数据

        if(query.getPageNo() != null) {
            searchRequest.source().from((query.getPageNo() - 1) * pageSize).size(pageSize);//    设置分页参数
        }
        if(query.getSortBy() != null  && !query.getSortBy().isBlank() && query.getIsAsc() != null) {
            searchRequest.source().sort(query.getSortBy(), query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC); // 排序
        }else {
            searchRequest.source().sort("updateTime", SortOrder.DESC); //默认排序
        }
//        //sort
//        List<OrderItem> orders = query.toMpPage("updateTime", false).orders(); // 获取排序字段列表
//        for (OrderItem order : orders) {
//            searchRequest.source().sort(order.getColumn(), order.isAsc() ? SortOrder.ASC : SortOrder.DESC);
//        }

        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        // 解析结果
        SearchHits hits = response.getHits(); // 获取搜索结果
        Long total = hits.getTotalHits().value; // 获取总记录数
        Long  pages = (total+pageSize-1) / pageSize; // 获取当前页码

        SearchHit[] searchHits = hits.getHits(); // 获取搜索结果
        if (searchHits == null) {
            return new PageDTO<>();
        }
        // 解析搜索结果
        List<ItemDTO> docs = parseHits(searchHits);
        // 封装并返回
        PageDTO<ItemDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(total);
        pageDTO.setPages(pages);
        pageDTO.setList(docs);
        return pageDTO;
    }

    /**
     * 解析搜索结果
     * @param hits
     * @return
     */

    private List<ItemDTO> parseHits(SearchHit[] hits) {
        if (hits == null || hits.length == 0) return Collections.emptyList();
        return Arrays.stream(hits)
                .map(hit -> JSONUtil.toBean(hit.getSourceAsString(), ItemDTO.class))
                .collect(Collectors.toList());
    }

//        List<ItemDoc> itemDocs = new ArrayList<>(); // 装载搜索结果
//        if(searchHits!=null){ // 防止搜索结果为空
//            for (SearchHit hit : searchHits) {
//                itemDocs.add(JSONUtil.toBean(hit.getSourceAsString(), ItemDoc.class));
//            }
//        }
//        //封装并返回
//        return new PageDTO<>(total, pages, itemDocs);
//    }


    @ApiOperation("搜索商品")
    @GetMapping("/{id}")
    public ItemDTO search(@PathVariable("id") Long id) throws IOException {
        //1.准备Request
        GetRequest request = new GetRequest("items",id.toString());
        //2.发送请求
        GetResponse response= client.get(request, RequestOptions.DEFAULT);
        //3.解析响应
        String item = response.getSourceAsString();
        //4.反序列化为对象
        ItemDoc itemDoc = JSONUtil.toBean(item, ItemDoc.class);
        ItemDTO itemDTO = BeanUtil.copyProperties(itemDoc, ItemDTO.class);
        // 5.返回
        return itemDTO;
    }
    @ApiOperation("分类聚合接口")
    @PostMapping("/filters")
    public CategoryAndBrandVo getFilters(@RequestBody ItemPageQuery query) {
        return searchService.getFilters(query);
    }
}
