package com.mscommerce.repositories;

import com.mscommerce.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, IOrderRepository {

    List<Order> findByIdCustomer(String idCustomer);

}
