package com.example.onlinestore.controller;

import com.example.onlinestore.annotation.RequireAdmin;
import com.example.onlinestore.annotation.ValidateParams;
import com.example.onlinestore.dto.CreateProductRequest;
import com.example.onlinestore.dto.ErrorResponse;
import com.example.onlinestore.dto.ProductPageRequest;
import com.example.onlinestore.model.Product;
import com.example.onlinestore.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Creates a new product.
     *
     * <p>This endpoint processes a product creation request by validating the input and creating the product via the business service.
     * On success, it returns a 200 OK response with the created product details. If the request is invalid, it returns a 400 Bad Request response
     * with an error message, and on other errors, it returns a 500 Internal Server Error response with a localized error message.</p>
     *
     * @param request the product creation request containing the necessary product details
     * @return a ResponseEntity containing the created product data or an error response if creation fails
     */
    @PostMapping
    @RequireAdmin
    @ValidateParams
    public ResponseEntity<?> createProduct(@RequestBody @Valid CreateProductRequest request) {
        try {
            logger.debug("开始创建商品，请求参数：{}", request);
            Product product = productService.createProduct(request);
            logger.debug("商品创建成功：{}", product.getName());
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            logger.warn("创建商品失败：{}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("创建商品失败：{}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                "error.system.internal", null, LocaleContextHolder.getLocale());
            return ResponseEntity.internalServerError().body(new ErrorResponse(errorMessage));
        }
    }

    /**
     * Retrieves a paginated list of products.
     *
     * <p>This method handles HTTP GET requests to fetch a list of products based on provided pagination parameters.
     * It returns a 200 OK response with the product list on success. If the provided parameters are invalid,
     * it responds with a 400 Bad Request and an error message. In case of other failures, it returns a 500 Internal Server Error
     * with a corresponding error response.</p>
     *
     * @param request the pagination parameters for the product query
     * @return a ResponseEntity containing the paginated product list or an error response
     */
    @GetMapping
    @ValidateParams
    public ResponseEntity<?> listProducts(@Valid ProductPageRequest request) {
        try {
            logger.debug("开始查询商品列表，请求参数：{}", request);
            return ResponseEntity.ok(productService.listProducts(request));
        } catch (IllegalArgumentException e) {
            logger.warn("查询商品列表失败：{}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("查询商品列表失败：{}", e.getMessage(), e);
            String errorMessage = messageSource.getMessage(
                "error.system.internal", null, LocaleContextHolder.getLocale());
            return ResponseEntity.internalServerError().body(new ErrorResponse(errorMessage));
        }
    }
} 