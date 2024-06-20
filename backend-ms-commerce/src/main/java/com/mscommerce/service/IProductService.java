package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.product.ProductDTO;
import com.mscommerce.models.DTO.product.ProductDTOGet;

import java.util.List;

public interface IProductService {

    List<ProductDTOGet> getAllProducts() throws ResourceNotFoundException;

    ProductDTOGet getProductById(Integer productId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByWineryId(Integer wineryId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByVarietyId(Integer varietyId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByTypeId(Integer typeId) throws ResourceNotFoundException;

    List<ProductDTOGet> findRandomProducts() throws ResourceNotFoundException;

    ProductDTO createProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException;

    ProductDTO updateProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException;

    void deleteProduct(Integer productId) throws ResourceNotFoundException;

}
