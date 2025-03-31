package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Brand;
import com.example.onlinestore.dto.BrandListQueryOptions;
import com.example.onlinestore.dto.Page;
import com.example.onlinestore.entity.BrandEntity;
import com.example.onlinestore.errors.ErrorCode;
import com.example.onlinestore.exceptions.BizException;
import com.example.onlinestore.mapper.BrandMapper;
import com.example.onlinestore.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Validated
public class BrandServiceImpl implements BrandService {
    private static final Logger logger = LoggerFactory.getLogger(BrandServiceImpl.class);

    private static final String DEFAULT_BRAND_LIST_QUERY_ORDERBY = "sort_score DESC";

    private final static Object BRAND_NAME_LOCK = new Object();

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public Brand getBrandById(@NotNull Long id) {
        BrandEntity brandEntity = brandMapper.findById(id);
        if (brandEntity == null) {
            logger.error("brand not found, id: {}", id);
            throw new BizException(ErrorCode.BRAND_NOT_FOUND);
        }

        return brandEntity.toBrand();
    }

    @Override
    public Page<Brand> listBrands(@NotNull @Valid BrandListQueryOptions options) {
        if (StringUtils.isNotBlank(options.getOrderBy())) {
            PageHelper.startPage(options.getPageNum(), options.getPageSize(), options.getOrderBy());
        } else {
            PageHelper.startPage(options.getPageNum(), options.getPageSize(), DEFAULT_BRAND_LIST_QUERY_ORDERBY);
        }

        List<BrandEntity> brandEntities = brandMapper.findAllBrands(options);
        if (brandEntities != null) {
            PageInfo<BrandEntity> pageInfo = new PageInfo<>(brandEntities);
            return Page.of(brandEntities.stream().map(BrandEntity::toBrand).toList(), pageInfo.getTotal(), options.getPageNum(), options.getPageSize());
        } else {
            return Page.of(List.of(), 0, options.getPageNum(), options.getPageSize());
        }
    }

    @Override
    public Brand tianJiaPingPai(@NotNull @Valid Brand brand) {
        // 品牌名称应该唯一
        synchronized (BRAND_NAME_LOCK) {
            String formatName = brand.getName().toUpperCase();
            BrandEntity brandEntity = brandMapper.findByName(formatName);
            if (brandEntity != null) {
                throw new BizException(ErrorCode.BRAND_NAME_DUPLICATED, brand.getName());
            }

            brandEntity = new BrandEntity();
            brandEntity.setName(formatName);
            brandEntity.setDescription(brand.getDescription());
            brandEntity.setLogo(brand.getLogo());
            brandEntity.setStory(brand.getStory());
            brandEntity.setSortScore(brand.getSortScore() == null ? 100 : brand.getSortScore());
            brandEntity.setShowStatus(brand.getShowStatus() == null ? 1 : brand.getShowStatus());
            LocalDateTime now = LocalDateTime.now();
            brandEntity.setCreatedAt(now);
            brandEntity.setUpdatedAt(now);

            int effectRows = brandMapper.insert(brandEntity);
            if (effectRows != 1) {
                logger.error("insert brand failed. because effect rows is 0. brandName:{}", brand.getName());
                throw new BizException(ErrorCode.INTERNAL_ERROR);
            }

            return brandEntity.toBrand();
        }
    }

    @Override
    public void delteBrand(@NotNull Long id) {
        synchronized (BRAND_NAME_LOCK) {
            BrandEntity brandEntity = brandMapper.findById(id);
            if (brandEntity == null) {
                logger.error("brand not found, id: {}", id);
                throw new BizException(ErrorCode.BRAND_NOT_FOUND);
            }

            int effectRows = brandMapper.deleteById(id);
            if (effectRows != 1) {
                logger.error("delete brand failed. because effect rows is 0. brandName:{}", brandEntity.getName());
                throw new BizException(ErrorCode.INTERNAL_ERROR);
            }
        }

    }


}
