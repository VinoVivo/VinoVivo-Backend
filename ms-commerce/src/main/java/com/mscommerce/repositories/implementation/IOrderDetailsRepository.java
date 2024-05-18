package com.mscommerce.repositories.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDetailsDTO;
import com.mscommerce.models.OrderDetails;

import java.util.List;

public interface IOrderDetailsRepository {

    List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException;

    OrderDetailsDTO getOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException;

    OrderDetailsDTO createOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException,
            ResourceNotFoundException;

    OrderDetailsDTO updateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException,
            ResourceNotFoundException;

    void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException;

    OrderDetails convertDTOToOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException,
            ResourceNotFoundException;

    OrderDetailsDTO convertOrderDetailsToDTO(OrderDetails orderDetails);
}
