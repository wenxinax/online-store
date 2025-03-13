package com.example.onlinestore.service;

import com.example.onlinestore.bean.Category;

import java.util.List;

/**
 * 商品类目服务
 */
public interface CategoryService {

    /**
     * 判断是否为根类目
     */
    boolean isRootCategory(Long categoryId);
    /**
     * 获取根类目
     */
    List<Category> getRootCategories();
    /**
     * 添加类目
     */
    void addCategory(Category category);
    
    /**
     * 根据ID获取类目
     */
    Category getCategoryById(Long id);
    
    /**
     * 更新类目
     */
    void updateCategory(Category category);
    
    /**
     * 删除类目
     */
    void deleteCategory(Long id);
    
    /**
     * 获取所有类目
     */
    List<Category> getAllCategories();
    
    /**
     * 获取子类目
     */
    List<Category> getChildCategories(Long parentId);
}