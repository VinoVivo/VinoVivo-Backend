package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.models.DTO.ProductDTOGet;
import com.mscommerce.service.implementation.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceImpl productServiceImpl;

    @GetMapping("/type/all")
    public ResponseEntity<List<ProductDTOGet>> getAllProducts() throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getAllProducts();
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/id/{productId}")
    public ResponseEntity<ProductDTOGet> getProductById(@PathVariable Integer productId) {
        ProductDTOGet productDTOGet = productServiceImpl.getProductById(productId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductDTOGet>> getRandomProducts() {
        List<ProductDTOGet> randomProducts = productServiceImpl.findRandomProducts();
        return ResponseEntity.ok(randomProducts);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) throws BadRequestException {
        ProductDTO createdProduct = productServiceImpl.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productServiceImpl.updateProduct(productDTO);
        return ResponseEntity.ok().body(updatedProduct);
    }

    @DeleteMapping("/delete/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer productId) throws ResourceNotFoundException {
        productServiceImpl.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/winery/{wineryId}")
    public ResponseEntity<List<ProductDTOGet>> getProductsByWineryId(@PathVariable Integer wineryId) {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByWineryId(wineryId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/variety/{varietyId}")
    public ResponseEntity<List<ProductDTOGet>> getProductsByVarietyId(@PathVariable Integer varietyId) {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByVarietyId(varietyId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<ProductDTOGet>> getProductsByTypeId(@PathVariable Integer typeId) {
        List<ProductDTOGet> productDTOGet = productServiceImpl.getProductsByTypeId(typeId);
        return ResponseEntity.ok().body(productDTOGet);
    }
}



