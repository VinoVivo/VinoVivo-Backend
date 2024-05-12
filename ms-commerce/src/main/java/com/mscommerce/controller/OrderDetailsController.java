package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDetailsDTO;
import com.mscommerce.service.OrderDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-details")
@RequiredArgsConstructor
public class OrderDetailsController {

    private final OrderDetailsService orderDetailsService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDetailsDTO>> getAllOrderDetails() throws ResourceNotFoundException {
        List<OrderDetailsDTO> orderDetailsDTOs = orderDetailsService.getAllOrderDetails();
        return ResponseEntity.ok().body(orderDetailsDTOs);
    }

    @GetMapping("/id/{orderDetailsId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetailsById(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        OrderDetailsDTO orderDetailsDTO = orderDetailsService.getOrderDetailsById(orderDetailsId);
        return ResponseEntity.ok().body(orderDetailsDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDetailsDTO> createOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO createdOrderDetails = orderDetailsService.createOrderDetails(orderDetailsDTO);
        return new ResponseEntity<>(createdOrderDetails, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<OrderDetailsDTO> updateOrderDetails(@RequestBody OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDetailsDTO updatedOrderDetails = orderDetailsService.updateOrderDetails(orderDetailsDTO);
        return ResponseEntity.ok().body(updatedOrderDetails);
    }

    @DeleteMapping("/delete/{orderDetailsId}")
    public ResponseEntity<Void> deleteOrderDetails(@PathVariable Integer orderDetailsId) throws ResourceNotFoundException {
        orderDetailsService.deleteOrderDetails(orderDetailsId);
        return ResponseEntity.noContent().build();
    }
}
