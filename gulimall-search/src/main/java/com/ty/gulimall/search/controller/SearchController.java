package com.ty.gulimall.search.controller;

import com.ty.gulimall.search.service.MallSearchService;
import com.ty.gulimall.search.vo.SearchParam;
import com.ty.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request){
        System.out.println("+++++++++++");
        SearchResult result = mallSearchService.search(searchParam);

        return "list";
    }
}
