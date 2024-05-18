package com.mscommerce.repositories.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDTO;
import com.mscommerce.models.Order;

import java.util.List;

public interface IOrderRepository {

    List<OrderDTO> getAllOrders() throws ResourceNotFoundException;

    OrderDTO getOrderById(Integer orderId) throws ResourceNotFoundException;

    OrderDTO createOrder(OrderDTO orderDTO) throws BadRequestException;

    OrderDTO updateOrder(OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException;

    void deleteOrder(Integer orderId) throws ResourceNotFoundException;

    Order convertOrderDTOToOrder(OrderDTO orderDTO) throws BadRequestException;

    OrderDTO convertOrderToOrderDTO(Order order);
}
