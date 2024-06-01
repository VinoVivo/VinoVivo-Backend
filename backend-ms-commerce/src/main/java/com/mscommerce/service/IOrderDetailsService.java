package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.OrderDetailsDTO;
import com.mscommerce.models.DTO.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.OrderDetailsDTOUpdate;
import com.mscommerce.models.OrderDetails;

import java.util.List;

public interface IOrderDetailsService {

    List<OrderDetailsDTO> adminGetAllOrderDetails() throws ResourceNotFoundException;

    List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException;

    List<OrderDetailsDTO> getOrderDetailsByOrderId(Integer orderId) throws ResourceNotFoundException, UnauthorizedAccessException;

    OrderDetailsDTO adminGetOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException;

    OrderDetailsDTO adminCreateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO createOrderDetailsToExistingOrder(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO adminUpdateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO updateOrderDetails(OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException;

    void adminForceDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException;

    void adminDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException;

    void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException, UnauthorizedAccessException;

    OrderDetails convertDTOToOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO convertOrderDetailsToDTO(OrderDetails orderDetails);
}
