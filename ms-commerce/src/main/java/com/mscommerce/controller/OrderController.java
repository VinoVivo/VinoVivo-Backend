package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDTO;
import com.mscommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() throws ResourceNotFoundException {
        List<OrderDTO> orderDTOs = orderService.getAllOrders();
        return ResponseEntity.ok().body(orderDTOs);
    }

    @GetMapping("/id/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) throws ResourceNotFoundException {
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        return ResponseEntity.ok().body(orderDTO);
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) throws BadRequestException {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<OrderDTO> updateOrder(@RequestBody OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException {
        OrderDTO updatedOrder = orderService.updateOrder(orderDTO);
        return ResponseEntity.ok().body(updatedOrder);
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) throws ResourceNotFoundException {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
