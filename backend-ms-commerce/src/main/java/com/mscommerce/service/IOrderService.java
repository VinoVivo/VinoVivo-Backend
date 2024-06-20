package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.order.OrderDTO;
import com.mscommerce.models.DTO.order.OrderDTORequest;
import com.mscommerce.models.DTO.order.OrderDTOUpdate;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface IOrderService {

    List<OrderDTO> adminGetAllOrders() throws ResourceNotFoundException;

    List<OrderDTO> getAllOrders() throws ResourceNotFoundException;

    OrderDTO adminGetOrderById(Integer orderId) throws ResourceNotFoundException;

    OrderDTO adminCreateOrder(OrderDTO orderDTO) throws BadRequestException;

    OrderDTO createOrder(OrderDTORequest orderDTORequest) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException;

    OrderDTO adminUpdateOrder(OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException;

    OrderDTO updateOrder(OrderDTOUpdate orderDTO) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException;

    void adminDeleteOrder(Integer orderId) throws ResourceNotFoundException;

    void deleteOrder(Integer orderId) throws ResourceNotFoundException, UnauthorizedAccessException, MessagingException, UnsupportedEncodingException;

}
