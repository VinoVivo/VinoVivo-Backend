package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTO;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOUpdate;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface IOrderDetailsService {

    List<OrderDetailsDTO> adminGetAllOrderDetails() throws ResourceNotFoundException;

    List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException;

    List<OrderDetailsDTO> getOrderDetailsByOrderId(Integer orderId) throws ResourceNotFoundException, UnauthorizedAccessException;

    OrderDetailsDTO adminGetOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException;

    OrderDetailsDTO adminCreateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO createOrderDetailsToExistingOrder(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException;

    OrderDetailsDTO adminUpdateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDetailsDTO updateOrderDetails(OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException, UnauthorizedAccessException, MessagingException, UnsupportedEncodingException;

    void adminForceDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException;

    void adminDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException;

    void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException, UnauthorizedAccessException, MessagingException, UnsupportedEncodingException;

}
