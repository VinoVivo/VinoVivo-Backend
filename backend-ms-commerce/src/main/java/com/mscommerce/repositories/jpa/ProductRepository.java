package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Product;
import com.mscommerce.repositories.interfaces.IProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, IProductRepository {

}

