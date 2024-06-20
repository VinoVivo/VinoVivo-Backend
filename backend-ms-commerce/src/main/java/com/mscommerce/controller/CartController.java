package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.cart.CartDTO;
import com.mscommerce.models.DTO.cart.CartDTORequest;
import com.mscommerce.models.DTO.cart.CartDTOUpdate;
import com.mscommerce.service.implementation.CartServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@Tag(name = "Cart Controller", description = "Handles all cart related operations")
public class CartController {

    final private CartServiceImpl cartServiceImpl;

    @GetMapping("/all-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch all carts for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all carts for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<CartDTO>> adminGetAllCarts() throws ResourceNotFoundException {
        List<CartDTO> cartDTOs = cartServiceImpl.adminGetAllCarts();
        return ResponseEntity.ok().body(cartDTOs);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Fetch all carts for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all carts for the current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<CartDTO>> getAllCarts() throws ResourceNotFoundException {
        List<CartDTO> cartDTOs = cartServiceImpl.getAllCarts();
        return ResponseEntity.ok().body(cartDTOs);
    }

    @GetMapping("/id/{cartId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch a specific cart by ID for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the cart with the provided ID for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<CartDTO> adminGetCartById(@PathVariable Integer cartId) throws ResourceNotFoundException {
        CartDTO cartDTO = cartServiceImpl.adminGetCartById(cartId);
        return ResponseEntity.ok().body(cartDTO);
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new cart for administrators", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new cart for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<CartDTO> adminCreateCart(@RequestBody CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException {
        CartDTO createdCart = cartServiceImpl.adminCreateCart(cartDTO);
        return new ResponseEntity<>(createdCart, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Create a new cart for the current user", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new cart for the current user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<CartDTO> createCart(@RequestBody CartDTORequest cartDTORequest) throws BadRequestException, ResourceNotFoundException {
        CartDTO createdCart = cartServiceImpl.createCart(cartDTORequest);
        return new ResponseEntity<>(createdCart, HttpStatus.CREATED);
    }

    @PutMapping("/update-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing cart for administrators", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing cart for administrators"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<CartDTO> adminUpdateCart(@RequestBody CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException {
        CartDTO updatedCart = cartServiceImpl.adminUpdateCart(cartDTO);
        return ResponseEntity.ok().body(updatedCart);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Update an existing cart for the current user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing cart for the current user"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<CartDTO> updateCart(@RequestBody CartDTOUpdate cartDTOUpdate) throws BadRequestException, ResourceNotFoundException {
        CartDTO updatedCart = cartServiceImpl.updateCart(cartDTOUpdate);
        return ResponseEntity.ok().body(updatedCart);
    }

    @DeleteMapping("/delete-admin/{cartId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific cart for administrators", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific cart for administrators"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> adminDeleteCart(@PathVariable Integer cartId) throws ResourceNotFoundException {
        cartServiceImpl.adminDeleteCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/{cartId}")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Delete a specific cart for the current user", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific cart for the current user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "404", description = "Cart not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteCart(@PathVariable Integer cartId) throws ResourceNotFoundException {
        cartServiceImpl.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clean")
    @PreAuthorize("hasRole('USER') OR hasRole('ADMIN')")
    @Operation(summary = "Clean the current user's cart", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully cleaned the current user's cart"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> cleanCart() {
        cartServiceImpl.cleanCart();
        return ResponseEntity.noContent().build();
    }
}
