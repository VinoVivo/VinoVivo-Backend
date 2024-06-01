package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDTO;
import com.mscommerce.models.DTO.OrderDTORequest;
import com.mscommerce.models.DTO.OrderDTOUpdate;
import com.mscommerce.models.Order;
import com.mscommerce.service.implementation.OrderServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderServiceImpl;

    @GetMapping("/all-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> adminGetAllOrders() throws ResourceNotFoundException {
        List<OrderDTO> orderDTOs = orderServiceImpl.adminGetAllOrders();
        return ResponseEntity.ok().body(orderDTOs);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() throws ResourceNotFoundException {
        // Retrieve all orders for the logged-in user
        List<OrderDTO> orderDTOs = orderServiceImpl.getAllOrders();
        return ResponseEntity.ok().body(orderDTOs);
    }

    @GetMapping("/id/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> adminGetOrderById(@PathVariable Integer orderId) {
        OrderDTO orderDTO = orderServiceImpl.adminGetOrderById(orderId);
        return ResponseEntity.ok().body(orderDTO);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> adminCreateOrder(@RequestBody OrderDTO orderDTO) throws BadRequestException {
        OrderDTO createdOrder = orderServiceImpl.adminCreateOrder(orderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTORequest orderDTORequest) throws BadRequestException {
        Order createdOrder = orderServiceImpl.createOrder(orderDTORequest);
        OrderDTO orderDTO = orderServiceImpl.convertOrderToOrderDTO(createdOrder);
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @PutMapping("/update-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> adminUpdateOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderServiceImpl.adminUpdateOrder(orderDTO);
        return ResponseEntity.ok().body(updatedOrder);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrder(@RequestBody OrderDTOUpdate orderDTO) {
        OrderDTO updatedOrderDTO = orderServiceImpl.updateOrder(orderDTO);
        return ResponseEntity.ok().body(updatedOrderDTO);
    }

    @DeleteMapping("/delete-admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteOrder(@PathVariable Integer orderId) {
        orderServiceImpl.adminDeleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        orderServiceImpl.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
