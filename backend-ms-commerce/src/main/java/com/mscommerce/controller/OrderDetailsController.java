package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTO;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOUpdate;
import com.mscommerce.service.implementation.OrderDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-details")
@Tag(name = "Order Details Controller", description = "Handles all order details related operations")
public class OrderDetailsController {

    private final OrderDetailsServiceImpl orderDetailsServiceImpl;

    @GetMapping("/all-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch all order details for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all order details for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<OrderDetailsDTO>> adminGetAllOrderDetails() throws ResourceNotFoundException {
        List<OrderDetailsDTO> orderDetailsDTOs = orderDetailsServiceImpl.adminGetAllOrderDetails();
        return ResponseEntity.ok().body(orderDetailsDTOs);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Fetch all order details for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all order details for the current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<OrderDetailsDTO>> getAllOrderDetails() throws ResourceNotFoundException {
        List<OrderDetailsDTO> userOrderDetails = orderDetailsServiceImpl.getAllOrderDetails();
        return ResponseEntity.ok().body(userOrderDetails);
    }

    @GetMapping("/all/order-id/{orderId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Fetch order details by order ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of order details for the given order ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<OrderDetailsDTO>> getOrderDetailsByOrderId(@PathVariable Integer orderId) throws ResourceNotFoundException {
        List<OrderDetailsDTO> orderDetails = orderDetailsServiceImpl.getOrderDetailsByOrderId(orderId);
        return ResponseEntity.ok().body(orderDetails);
    }

    @GetMapping("/id/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch a specific order detail by ID for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order detail with the provided ID for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDetailsDTO> adminGetOrderDetailsById(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        OrderDetailsDTO orderDetailsDTO = orderDetailsServiceImpl.adminGetOrderDetailsById(orderDetailsId);
        return ResponseEntity.ok().body(orderDetailsDTO);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new order detail for administrators", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new order detail for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDetailsDTO> adminCreateOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO createdOrderDetails = orderDetailsServiceImpl.adminCreateOrderDetails(orderDetailsDTO);
        return new ResponseEntity<>(createdOrderDetails, HttpStatus.CREATED);
    }

    @PostMapping("/create-to-existing-order")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Create a new order detail for an existing order", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new order detail for an existing order"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDetailsDTO> createOrderDetailsToExistingOrder(@RequestBody OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        OrderDetailsDTO createdOrderDetails = orderDetailsServiceImpl.createOrderDetailsToExistingOrder(orderDetailsDTOAddition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrderDetails);
    }

    @PutMapping("/update-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing order detail for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing order detail for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDetailsDTO> adminUpdateOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO updatedOrderDetails = orderDetailsServiceImpl.adminUpdateOrderDetails(orderDetailsDTO);
        return ResponseEntity.ok().body(updatedOrderDetails);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Update an existing order detail for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing order detail for the current user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<OrderDetailsDTO> updateOrderDetails(@RequestBody OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        OrderDetailsDTO updatedOrderDetails = orderDetailsServiceImpl.updateOrderDetails(orderDetailsDTOUpdate);
        return ResponseEntity.ok().body(updatedOrderDetails);
    }

    @DeleteMapping("/force-delete-admin/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force delete a specific order detail for administrators", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully force deleted the specific order detail for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> adminForceDeleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsServiceImpl.adminForceDeleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-admin/{orderDetailsId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific order detail for administrators", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific order detail for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> adminDeleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsServiceImpl.adminDeleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{orderDetailsId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Delete a specific order detail for the current user", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific order detail for the current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Order detail not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        orderDetailsServiceImpl.deleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }
}
