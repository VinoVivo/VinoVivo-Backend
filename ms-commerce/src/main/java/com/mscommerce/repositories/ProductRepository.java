package com.mscommerce.repositories;

import com.mscommerce.models.DTO.ProductDTOGet;
import com.mscommerce.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    //Retrieves a list of ProductDTOGet objects containing information about all products,
    //including their type, variety, and winery.
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id")
    List<ProductDTOGet> findAllProductDTOGet();

     // Retrieves a ProductDTOGet object containing information about a product specified by its ID,
     // including its type, variety, and winery.
     // @param productId The ID of the product to retrieve.
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
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
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
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
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
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
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "WHERE t.id = :typeId")
    List<ProductDTOGet> findProductsByTypeId(@Param("typeId") Integer typeId);

    // Retrieves a page of ProductDTOGet objects containing information about products, ordered randomly.
    // @param pageable Pagination information.
    @Query("SELECT NEW com.mscommerce.models.DTO.ProductDTOGet(p.id, p.name, p.description, p.image, p.year, p.price," +
            " p.stock, w.name, v.name, t.name) " +
            "FROM Product p " +
            "JOIN Winery w ON p.winery.id = w.id " +
            "JOIN Variety v ON p.variety.id = v.id " +
            "JOIN Type t ON p.type.id = t.id " +
            "ORDER BY RAND()")
    Page<ProductDTOGet> findRandProductDTOs(Pageable pageable);
}
