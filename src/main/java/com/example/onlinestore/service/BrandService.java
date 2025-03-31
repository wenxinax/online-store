package com.example.onlinestore.service;

import com.example.onlinestore.bean.Brand;
import com.example.onlinestore.dto.BrandListQueryOptions;
import com.example.onlinestore.dto.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface BrandService {
    // 根据id查询品牌
    Brand getBrandById(@NotNull Long id);
    // 分页查询品牌
    Page<Brand> listBrands(@NotNull @Valid BrandListQueryOptions options);
    //添加一个品牌
    Brand  tianJiaPingPai(@NotNull @Valid Brand brand);

    // 删除一个品牌
    void delteBrand(@NotNull  Long id);
}
