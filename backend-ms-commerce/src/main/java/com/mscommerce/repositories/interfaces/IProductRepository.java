package com.mscommerce.repositories.interfaces;

import com.mscommerce.models.DTO.product.ProductDTOGet;
import com.mscommerce.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IProductRepository {

    //Retrieves a list of ProductDTOGet objects containing information about all products,
    //including their type, variety, and winery.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id")
    List<ProductDTOGet> findAllProductDTOGet();

    // Retrieves a ProductDTOGet object containing information about a product specified by its ID,
    // including its type, variety, and winery.
    // @param productId The ID of the product to retrieve.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "WHERE p.id = :productId")
    ProductDTOGet findProductDTOGetById(@Param("productId") Integer productId);

    // Retrieves a list of ProductDTOGet objects containing information about products
    // produced by a specific winery, including their type, variety, and winery.
    // @param wineryId The ID of the winery.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "WHERE w.id = :wineryId")
    List<ProductDTOGet> findProductsByWineryId(@Param("wineryId") Integer wineryId);

    // Retrieves a list of ProductDTOGet objects containing information about products
    // of a specific variety, including their type, variety, and winery.
    // @param varietyId The ID of the variety.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "WHERE v.id = :varietyId")
    List<ProductDTOGet> findProductsByVarietyId(@Param("varietyId") Integer varietyId);

    //Retrieves a list of ProductDTOGet objects containing information about products
    // of a specific type, including their type, variety, and winery.
    // @param typeId The ID of the type.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "WHERE t.id = :typeId")
    List<ProductDTOGet> findProductsByTypeId(@Param("typeId") Integer typeId);

    // Retrieves a page of ProductDTOGet objects containing information about products, ordered randomly.
    // @param pageable Pagination information.
    @Query("SELECT NEW com.mscommerce.models.DTO.product.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "ORDER BY RAND()")
    Page<ProductDTOGet> findRandProductDTOs(Pageable pageable);

    // Retrieves the price of a product specified by its ID.
    @Query("SELECT p.price FROM Product p WHERE p.id = :id")
    Double findPriceById(Integer id);

    // Retrieves a list of products with stock less than the specified amount, ordered by stock in ascending order.
    @Query("SELECT p FROM Product p JOIN FETCH p.winery " +
            "WHERE p.stock < :stock " +
            "ORDER BY p.stock ASC")
    List<Product> findAllByStockLessThan(Integer stock);

    // Retrieves a list of products and their stock, ordered by stock in descending order and the count of order details in descending order.
    @Query("SELECT p, COUNT(od), SUM(p.stock) " +
            "FROM Product p JOIN FETCH p.winery JOIN p.orderDetails od " +
            "GROUP BY p ORDER BY SUM(p.stock) DESC, COUNT(od) DESC")
    List<Object[]> findTop10ByOrderDetailsCountAndSumStock(Pageable pageable);

    // Retrieves a list of product types and their stock, filtered by type ID.
    @Query("SELECT p.type, COUNT(od), SUM(p.stock) " +
            "FROM OrderDetails od JOIN od.product p " +
            "WHERE p.type.id = :typeId GROUP BY p.type")
    List<Object[]> countByProductTypeAndSumStock(Integer typeId);

    // Retrieves a list of products and the sum of their quantities in order details, filtered by year and type ID, ordered by the sum of quantities in descending order.
    @Query("SELECT p, SUM(od.quantity) " +
            "FROM Product p JOIN OrderDetails od ON p.id = od.product.id " +
            "WHERE (:year IS NULL OR :year = 0 OR p.year = :year) " +
            "AND (:typeId IS NULL OR :typeId = 0 OR p.type.id = :typeId) " +
            "GROUP BY p " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findAllProductsByYearAndTypeIdAndOrderDetailsQuantitySumDesc(@Param("year") Integer year, @Param("typeId") Integer typeId);

    // Retrieves a list of product types and the sum of their quantities in order details, filtered by year and type ID, ordered by the sum of quantities in descending order.
    @Query("SELECT p.type, SUM(od.quantity) " +
            "FROM Product p JOIN OrderDetails od ON p.id = od.product.id " +
            "WHERE (:year IS NULL OR :year = 0 OR p.year = :year) " +
            "AND (:typeId IS NULL OR :typeId = 0 OR p.type.id = :typeId) " +
            "GROUP BY p.type " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> findAllTypesByYearAndTypeIdAndOrderDetailsQuantitySumDesc(@Param("year") Integer year, @Param("typeId") Integer typeId);

    // Retrieves a list of products and the sum of their total sales (quantity times price), filtered by year and type ID, ordered by the sum of total sales in descending order.
    @Query("SELECT p, SUM(od.quantity * p.price) " +
            "FROM Product p JOIN OrderDetails od ON p.id = od.product.id " +
            "WHERE (:year IS NULL OR :year = 0 OR p.year = :year) " +
            "AND (:typeId IS NULL OR :typeId = 0 OR p.type.id = :typeId) " +
            "GROUP BY p " +
            "ORDER BY SUM(od.quantity * p.price) DESC")
    List<Object[]> findAllByYearAndTypeIdAndTotalSalesDesc(@Param("year") Integer year, @Param("typeId") Integer typeId);

    // Retrieves a list of products and their stock, filtered by year and type ID.
    @Query("SELECT p, p.stock " +
            "FROM Product p " +
            "WHERE (:year IS NULL OR :year = 0 OR p.year = :year) " +
            "AND (:typeId IS NULL OR :typeId = 0 OR p.type.id = :typeId)")
    List<Object[]> findAllByYearAndTypeId(@Param("year") Integer year, @Param("typeId") Integer typeId);
}
