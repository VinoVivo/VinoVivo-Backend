/*package com.msusers.repositories;

import com.msusers.models.DTO.OrderDTO;
import com.msusers.security.feign.FeignInterceptor;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "ms-commerce", url = "http://localhost:8082", configuration = FeignInterceptor.class)
public interface IFeignCommerceOrderRepository {

    @GetMapping(value = "/order/all")
    ResponseEntity<List<OrderDTO>> getAllOrders();

    @PostMapping(value = "/order/create")
    ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO);

    default ResponseEntity<List<OrderDTO>> getAllOrdersWithExceptionHandling() {
        try {
            return getAllOrders();
        } catch (FeignException e) {
            // Handle FeignException here
            // For example, return ResponseEntity with appropriate status code and error message
            return ResponseEntity.status(e.status()).body(null); // Modify this line as per your error handling logic
        }
    }
}*/
