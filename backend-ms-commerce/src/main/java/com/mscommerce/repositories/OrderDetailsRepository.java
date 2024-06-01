package com.mscommerce.repositories;

import com.mscommerce.models.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer>, IOrderDetailsRepository {

    List<OrderDetails> findByIdOrderIn(List<Integer> idOrders);

    List<OrderDetails> findAllByIdOrder(Integer orderId);
}
