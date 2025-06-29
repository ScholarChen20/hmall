package com.hmall.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.common.domain.PageDTO;
import com.hmall.item.domain.query.ItemPageQuery;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.vo.CategoryAndBrandVo;

import java.io.IOException;

public interface ISearchService extends IService<Item> {

    PageDTO<ItemDoc> EsSearch(com.hmall.search.domain.query.ItemPageQuery query) throws IOException;

    CategoryAndBrandVo getFilters(com.hmall.search.domain.query.ItemPageQuery query);
}