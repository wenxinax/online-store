package com.example.onlinestore.service;

import com.example.onlinestore.dto.CreateProductRequest;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.ProductPageRequest;
import com.example.onlinestore.model.Product;

public interface ProductService {
    /**
 * Creates a new product based on the provided creation request.
 *
 * @param request the product creation request containing details for the new product
 * @return the newly created product
 */
Product createProduct(CreateProductRequest request);
    /**
 * Retrieves a paginated list of products based on the provided criteria.
 *
 * @param request the request containing pagination and filter criteria for listing products
 * @return a paginated response containing products and related pagination details
 */
PageResponse<Product> listProducts(ProductPageRequest request);
} 