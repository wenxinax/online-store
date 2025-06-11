package com.example.onlinestore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Retrieves the unique identifier for the product.
     *
     * @return the product's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this product.
     *
     * @param id the unique identifier to assign to the product
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieves the product name.
     *
     * @return the product name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product's name.
     *
     * @param name the new name for the product
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product's category.
     *
     * @return the category of the product
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category for this product.
     *
     * @param category the category to assign to this product
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the price of the product.
     *
     * @return the product's price as a BigDecimal.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the product's price.
     *
     * @param price the new price for the product as a BigDecimal.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the timestamp when the product was created.
     *
     * @return the creation timestamp of the product
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of this product.
     *
     * @param createdAt the date and time when the product was created
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the timestamp indicating when the product was last updated.
     *
     * @return the LocalDateTime representing the last update time
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp for when the product was last updated.
     *
     * @param updatedAt the new timestamp indicating the product's last update
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 