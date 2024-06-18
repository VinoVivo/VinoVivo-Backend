package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.order.OrderDTO;
import com.mscommerce.models.DTO.order.OrderDTORequest;
import com.mscommerce.models.DTO.order.OrderDTOUpdate;
import com.mscommerce.service.implementation.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
@Tag(name = "Order Controller", description = "Handles all order related operations")
public class OrderController {

    private final OrderServiceImpl orderServiceImpl;

    @GetMapping("/all-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch all orders for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all orders for administrators"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<OrderDTO>> adminGetAllOrders() throws ResourceNotFoundException {
        List<OrderDTO> orderDTOs = orderServiceImpl.adminGetAllOrders();
        return ResponseEntity.ok().body(orderDTOs);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Fetch all orders for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all orders for the current user"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<OrderDTO>> getAllOrders() throws ResourceNotFoundException {
        List<OrderDTO> orderDTOs = orderServiceImpl.getAllOrders();
        return ResponseEntity.ok().body(orderDTOs);
    }

    @GetMapping("/id/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch a specific order by ID for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order with the provided ID for administrators"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDTO> adminGetOrderById(@PathVariable Integer orderId) throws ResourceNotFoundException {
        OrderDTO orderDTO = orderServiceImpl.adminGetOrderById(orderId);
        return ResponseEntity.ok().body(orderDTO);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new order for administrators", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new order for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDTO> adminCreateOrder(@RequestBody OrderDTO orderDTO) throws BadRequestException {
        OrderDTO createdOrder = orderServiceImpl.adminCreateOrder(orderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Create a new order for the current user", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new order for the current user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTORequest orderDTORequest) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        OrderDTO createdOrderDTO = orderServiceImpl.createOrder(orderDTORequest);
        return new ResponseEntity<>(createdOrderDTO, HttpStatus.CREATED);
    }

    @PutMapping("/update-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing order for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing order for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDTO> adminUpdateOrder(@RequestBody OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDTO updatedOrder = orderServiceImpl.adminUpdateOrder(orderDTO);
        return ResponseEntity.ok().body(updatedOrder);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Update an existing order for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing order for the current user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDTO> updateOrder(@RequestBody OrderDTOUpdate orderDTO) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        OrderDTO updatedOrderDTO = orderServiceImpl.updateOrder(orderDTO);
        return ResponseEntity.ok().body(updatedOrderDTO);
    }

    @DeleteMapping("/delete-admin/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific order for administrators", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific order for administrators"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> adminDeleteOrder(@PathVariable Integer orderId) throws ResourceNotFoundException {
        orderServiceImpl.adminDeleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{orderId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Delete a specific order for the current user", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific order for the current user"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) throws ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        orderServiceImpl.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
