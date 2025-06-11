package com.example.onlinestore.mapper;

import com.example.onlinestore.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    /**
 * Inserts a new product record into the database.
 *
 * @param product the product to be inserted
 */
void insertProduct(Product product);
    
    /**
                                     * Retrieves a paginated list of products that match the specified name filter.
                                     *
                                     * This method returns a subset of products starting at the provided offset and constrained to the specified limit.
                                     *
                                     * @param name the product name filter for searching products
                                     * @param offset the starting index for pagination
                                     * @param limit the maximum number of products to retrieve
                                     * @return a list of products that meet the search criteria with pagination applied
                                     */
                                    List<Product> findWithPagination(@Param("name") String name, 
                                    @Param("offset") int offset, 
                                    @Param("limit") int limit);
    
    /**
 * Counts the total number of products matching the specified name.
 *
 * @param name the product name filter applied to the count
 * @return the total count of matching products
 */
long countTotal(@Param("name") String name);

    /**
 * Retrieves all product records from the database.
 *
 * @return a list containing every product
 */
List<Product> findAll();
} 