package com.example.onlinestore.controller;

import com.example.onlinestore.bean.Brand;
import com.example.onlinestore.dto.BrandListQueryOptions;
import com.example.onlinestore.dto.Page;
import com.example.onlinestore.dto.Response;
import com.example.onlinestore.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("")
    public Response<Page<Brand>> listBrands(@RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        BrandListQueryOptions options = new BrandListQueryOptions();
        options.setPageNum(pageNum);
        options.setPageSize(pageSize);
        Page<Brand> brands = brandService.listBrands(options);
        return Response.success(brands);
    }
}
