package com.mscommerce.repositories.jpa;

import com.mscommerce.models.OrderDetails;
import com.mscommerce.repositories.interfaces.IOrderDetailsRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer>, IOrderDetailsRepository {

    List<OrderDetails> findByIdOrderIn(List<Integer> idOrders);

    List<OrderDetails> findAllByIdOrder(Integer orderId);
}
