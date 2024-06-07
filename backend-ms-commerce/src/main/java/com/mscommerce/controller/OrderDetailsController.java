package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTO;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOUpdate;
import com.mscommerce.service.implementation.OrderDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-details")
@RequiredArgsConstructor
public class OrderDetailsController {

    private final OrderDetailsServiceImpl orderDetailsServiceImpl;

    @GetMapping("/all-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDetailsDTO>> adminGetAllOrderDetails() throws ResourceNotFoundException {
        List<OrderDetailsDTO> orderDetailsDTOs = orderDetailsServiceImpl.adminGetAllOrderDetails();
        return ResponseEntity.ok().body(orderDetailsDTOs);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<List<OrderDetailsDTO>> getAllOrderDetails() throws ResourceNotFoundException {
        List<OrderDetailsDTO> userOrderDetails = orderDetailsServiceImpl.getAllOrderDetails();
        return ResponseEntity.ok().body(userOrderDetails);
    }

    @GetMapping("/all/order-id/{orderId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<List<OrderDetailsDTO>> getOrderDetailsByOrderId(@PathVariable Integer orderId) throws ResourceNotFoundException {
        List<OrderDetailsDTO> orderDetails = orderDetailsServiceImpl.getOrderDetailsByOrderId(orderId);
        return ResponseEntity.ok().body(orderDetails);
    }

    @GetMapping("/id/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailsDTO> adminGetOrderDetailsById(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        OrderDetailsDTO orderDetailsDTO = orderDetailsServiceImpl.adminGetOrderDetailsById(orderDetailsId);
        return ResponseEntity.ok().body(orderDetailsDTO);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailsDTO> adminCreateOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO createdOrderDetails = orderDetailsServiceImpl.adminCreateOrderDetails(orderDetailsDTO);
        return new ResponseEntity<>(createdOrderDetails, HttpStatus.CREATED);
    }

    @PostMapping("/create-to-existing-order")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<OrderDetailsDTO> createOrderDetailsToExistingOrder(@RequestBody OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO createdOrderDetails = orderDetailsServiceImpl.createOrderDetailsToExistingOrder(orderDetailsDTOAddition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrderDetails);
    }

    @PutMapping("/update-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailsDTO> adminUpdateOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO updatedOrderDetails = orderDetailsServiceImpl.adminUpdateOrderDetails(orderDetailsDTO);
        return ResponseEntity.ok().body(updatedOrderDetails);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<OrderDetailsDTO> updateOrderDetails(@RequestBody OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO updatedOrderDetails = orderDetailsServiceImpl.updateOrderDetails(orderDetailsDTOUpdate);
        return ResponseEntity.ok().body(updatedOrderDetails);
    }

    @DeleteMapping("/force-delete-admin/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminForceDeleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsServiceImpl.adminForceDeleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-admin/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsServiceImpl.adminDeleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{orderDetailsId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsServiceImpl.deleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }
}