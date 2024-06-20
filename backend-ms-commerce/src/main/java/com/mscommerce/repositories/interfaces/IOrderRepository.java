package com.mscommerce.repositories.interfaces;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface IOrderRepository {

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.totalPrice = :totalPrice WHERE o.id = :orderId")
    void updateTotalPriceById(@Param("totalPrice") Double totalPrice, @Param("orderId") Integer orderId);

}
