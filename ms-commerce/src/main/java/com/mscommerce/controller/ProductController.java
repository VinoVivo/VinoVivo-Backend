package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts() throws ResourceNotFoundException {
        List<ProductDTO> productDTOs = productService.getAllProducts();
        return ResponseEntity.ok().body(productDTOs);
    }

    @GetMapping("/id/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer productId) throws ResourceNotFoundException {
        ProductDTO productDTO = productService.getProductById(productId);
        return ResponseEntity.ok().body(productDTO);
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductDTO>> getRandomProducts() throws ResourceNotFoundException {
        List<ProductDTO> randomProducts = productService.findRandomProducts();
        return ResponseEntity.ok(randomProducts);
    }

    @PostMapping("/create")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        ProductDTO updatedProduct = productService.updateProduct(productDTO);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) throws ResourceNotFoundException {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/winery/{wineryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByWineryId(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        List<ProductDTO> products = productService.getProductsByWineryId(wineryId);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/variety/{varietyId}")
    public ResponseEntity<List<ProductDTO>> getProductsByVarietyId(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        List<ProductDTO> products = productService.getProductsByVarietyId(varietyId);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<ProductDTO>> getProductsByTypeId(@PathVariable Integer typeId) throws ResourceNotFoundException {
        List<ProductDTO> products = productService.getProductsByTypeId(typeId);
        return ResponseEntity.ok().body(products);
    }
}



