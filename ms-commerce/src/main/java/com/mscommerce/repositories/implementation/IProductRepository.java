package com.mscommerce.repositories.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.models.DTO.ProductDTOGet;
import com.mscommerce.models.Product;

import java.util.List;

public interface IProductRepository {

    List<ProductDTOGet> getAllProducts() throws ResourceNotFoundException;

    ProductDTOGet getProductById(Integer productId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByWineryId(Integer wineryId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByVarietyId(Integer varietyId) throws ResourceNotFoundException;

    List<ProductDTOGet> getProductsByTypeId(Integer typeId) throws ResourceNotFoundException;

    List<ProductDTOGet> findRandomProducts() throws ResourceNotFoundException;

    ProductDTO createProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException;

    ProductDTO updateProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException;

    void deleteProduct(Integer productId) throws ResourceNotFoundException;

    Product convertProductDTOToProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException;

    ProductDTO convertProductToProductDTO(Product product);
}
