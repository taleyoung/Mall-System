package com.ty.gulimall.search.service;

import com.ty.gulimall.search.vo.SearchParam;
import com.ty.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    SearchResult search(SearchParam searchParam);
}
