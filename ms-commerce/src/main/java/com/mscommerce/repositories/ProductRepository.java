package com.mscommerce.repositories;

import com.mscommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT * FROM products ORDER BY RAND() LIMIT 8", nativeQuery = true)
    List<Product> findRandProducts();
    List<Product> findByWineryId(Integer wineryId);

    List<Product> findByVarietyId(Integer varietyId);

    List<Product> findByTypeId(Integer typeId);
}
