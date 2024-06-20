package com.mscommerce.repositories.interfaces;

import com.mscommerce.models.OrderDetails;

import java.util.List;

public interface IOrderDetailsRepository {

    List<OrderDetails> findByIdOrder(Integer idOrder);

}
