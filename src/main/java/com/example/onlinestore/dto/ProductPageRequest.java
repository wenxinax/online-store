package com.example.onlinestore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ProductPageRequest {
    @Min(value = 1, message = "error.page.number.min")
    private int pageNum = 1;

    @Min(value = 1, message = "error.page.size.min")
    @Max(value = 100, message = "error.page.size.max")
    private int pageSize = 10;

    private String name;

    /**
     * Returns the current page number for the pagination request.
     *
     * @return the current page number, which is at least 1
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * Sets the page number for pagination.
     *
     * @param pageNum the new page number (must be at least 1)
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * Returns the number of items per page for the pagination request.
     *
     * @return the current page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Updates the page size for pagination requests.
     *
     * @param pageSize the number of items per page; expected to be between 1 and 100.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Returns the name filter used for querying products.
     *
     * @return the product name filter, or null if it is not set
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the filter value for the product name.
     *
     * @param name the string used to filter products by name
     */
    public void setName(String name) {
        this.name = name;
    }
} 