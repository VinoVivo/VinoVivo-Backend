package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Product;
import com.mscommerce.repositories.interfaces.IProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, IProductRepository {

    @Query("SELECT p.price FROM Product p WHERE p.id = :id")
    Double findPriceById(Integer id);

    @Query("SELECT p FROM Product p JOIN FETCH p.winery WHERE p.stock < :stock ORDER BY p.stock ASC")
    List<Product> findAllByStockLessThan(Integer stock);

    @Query("SELECT p, COUNT(od), SUM(p.stock) FROM Product p JOIN FETCH p.winery JOIN p.orderDetails od GROUP BY p ORDER BY SUM(p.stock) DESC, COUNT(od) DESC")
    List<Object[]> findTop10ByOrderDetailsCountAndSumStock(Pageable pageable);

    @Query("SELECT p.type, COUNT(od), SUM(p.stock) FROM OrderDetails od JOIN od.product p WHERE p.type.id = :typeId GROUP BY p.type")
    List<Object[]> countByProductTypeAndSumStock(Integer typeId);

}
