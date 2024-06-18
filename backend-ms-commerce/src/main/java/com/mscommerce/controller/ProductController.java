package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.product.ProductDTO;
import com.mscommerce.models.DTO.product.ProductDTOGet;
import com.mscommerce.models.Product;
import com.mscommerce.service.implementation.ProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@Tag(name = "Product Controller", description = "Handles all product related operations")
public class ProductController {

    private final ProductServiceImpl productServiceImpl;

    @GetMapping("/stock/less/{stock}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch all products with stock less than a certain amount", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all products with stock less than the given amount"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<Map<String, Object>>> getAllByStockLessThan(@PathVariable Integer stock) {
        List<Map<String, Object>> products = productServiceImpl.getAllByStockLessThan(stock);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/top10")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch top 10 products by order details count and sum stock", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of top 10 products by order details count and sum stock"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<Map<String, Object>>> getTop10ByOrderDetailsCountAndSumStock() {
        List<Map<String, Object>> products = productServiceImpl.getTop10ByOrderDetailsCountAndSumStock();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/type/count/{typeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch product count by product type and sum stock", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the product count by product type and sum stock"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<Map<String, Object>>> getCountByProductTypeAndSumStock(@PathVariable Integer typeId) {
        List<Map<String, Object>> counts = productServiceImpl.getCountByProductTypeAndSumStock(typeId);
        return new ResponseEntity<>(counts, HttpStatus.OK);
    }

    @GetMapping("/type/all")
    @Operation(summary = "Fetch all products", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of all products"),
            @ApiResponse(responseCode = "404", description = "Products not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<ProductDTOGet>> getAllProducts() throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getAllProducts();
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/id/{productId}")
    @Operation(summary = "Fetch product by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the product with the provided ID"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<ProductDTOGet> getProductById(@PathVariable Integer productId) throws ResourceNotFoundException {
        ProductDTOGet productDTOGet = productServiceImpl.getProductById(productId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/random")
    @Operation(summary = "Fetch random products", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a list of random products"),
            @ApiResponse(responseCode = "404", description = "Products not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<ProductDTOGet>> getRandomProducts() throws ResourceNotFoundException {
        List<ProductDTOGet> randomProducts = productServiceImpl.findRandomProducts();
        return ResponseEntity.ok(randomProducts);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", responses = {
            @ApiResponse(responseCode = "201", description = "Successfully created a new product"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        ProductDTO createdProduct = productServiceImpl.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing product", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the existing product"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        ProductDTO updatedProduct = productServiceImpl.updateProduct(productDTO);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a specific product", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the specific product"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) throws ResourceNotFoundException {
        productServiceImpl.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/winery/{wineryId}")
    @Operation(summary = "Fetch products by winery ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products for the given winery ID"),
            @ApiResponse(responseCode = "404", description = "Products not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<ProductDTOGet>> getProductsByWineryId(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByWineryId(wineryId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/variety/{varietyId}")
    @Operation(summary = "Fetch products by variety ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products for the given variety ID"),
            @ApiResponse(responseCode = "404", description = "Products not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<ProductDTOGet>> getProductsByVarietyId(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByVarietyId(varietyId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/type/{typeId}")
    @Operation(summary = "Fetch products by type ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of products for the given type ID"),
            @ApiResponse(responseCode = "404", description = "Products not found"),
            @ApiResponse(responseCode = "500", description = "An error occurred while processing the request")
    })
    public ResponseEntity<List<ProductDTOGet>> getProductsByTypeId(@PathVariable Integer typeId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByTypeId(typeId);
        return ResponseEntity.ok().body(productDTOGet);
    }
}



