package com.example.onlinestore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateProductRequest {
    @NotBlank(message = "error.product.name.empty")
    private String name;

    @NotBlank(message = "error.product.category.empty")
    private String category;

    @NotNull(message = "error.product.price.empty")
    @DecimalMin(value = "0.01", message = "error.product.price.min")
    private BigDecimal price;

    /**
     * Retrieves the name of the product.
     *
     * @return the product's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product's name.
     *
     * @param name the non-blank name to assign to the product
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the product category.
     *
     * @return the current product category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the product's category.
     *
     * @param category the category name to set; expected to be non-blank.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the price associated with this product request.
     *
     * @return the price as a BigDecimal
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the product.
     *
     * @param price the new price to assign; it should be a non-null BigDecimal with a minimum value of 0.01
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
} 