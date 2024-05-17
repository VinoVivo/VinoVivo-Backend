package com.mscommerce.controller;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.models.DTO.ProductDTOGet;
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
    public ResponseEntity<List<ProductDTOGet>> getAllProducts() throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productService.getAllProducts();
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/id/{productId}")
    public ResponseEntity<ProductDTOGet> getProductById(@PathVariable Integer productId) throws ResourceNotFoundException {
        ProductDTOGet productDTOGet = productService.getProductById(productId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductDTOGet>> getRandomProducts() throws ResourceNotFoundException {
        List<ProductDTOGet> randomProducts = productService.findRandomProducts();
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
    public ResponseEntity<List<ProductDTOGet>> getProductsByWineryId(@PathVariable Integer wineryId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productService.getProductsByWineryId(wineryId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/variety/{varietyId}")
    public ResponseEntity<List<ProductDTOGet>> getProductsByVarietyId(@PathVariable Integer varietyId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productService.getProductsByVarietyId(varietyId);
        return ResponseEntity.ok().body(productDTOGet);
    }

    @GetMapping("/type/{typeId}")
    public ResponseEntity<List<ProductDTOGet>> getProductsByTypeId(@PathVariable Integer typeId) throws ResourceNotFoundException {
        List<ProductDTOGet> productDTOGet = productService.getProductsByTypeId(typeId);
        return ResponseEntity.ok().body(productDTOGet);
    }
}



