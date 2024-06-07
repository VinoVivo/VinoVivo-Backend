package com.mscommerce.repositories.jpa;

import com.mscommerce.models.Cart;
import com.mscommerce.repositories.interfaces.IOrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer>, IOrderRepository {

    List<Cart> findByIdCustomer(String idCustomer);

    Long countByIdCustomer(String idCustomer);

}
