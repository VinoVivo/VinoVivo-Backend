package com.mscommerce.repositories;

import com.mscommerce.models.OrderDetails;

import java.util.List;

public interface IOrderDetailsRepository {

    List<OrderDetails> findByIdOrder(Integer idOrder);

}
