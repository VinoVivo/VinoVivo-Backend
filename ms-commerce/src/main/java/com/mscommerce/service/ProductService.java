package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.models.DTO.ProductDTOGet;
import com.mscommerce.models.Product;
import com.mscommerce.models.Type;
import com.mscommerce.models.Variety;
import com.mscommerce.models.Winery;
import com.mscommerce.repositories.ProductRepository;
import com.mscommerce.repositories.TypeRepository;
import com.mscommerce.repositories.VarietyRepository;
import com.mscommerce.repositories.WineryRepository;
import com.mscommerce.repositories.implementation.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductRepository {

    private final ProductRepository productRepository;

    private final WineryRepository wineryRepository;

    private final VarietyRepository varietyRepository;

    private final TypeRepository typeRepository;

    // Method to fetch all products
    @Override
    public List<ProductDTOGet> getAllProducts() throws ResourceNotFoundException {
        try {
            // Retrieve all products from the repository and return them
            return productRepository.findAllProductDTOGet();
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch products");
        }
    }

    // Method to fetch a product by ID
    @Override
    public ProductDTOGet getProductById(Integer productId) throws ResourceNotFoundException {
        try {
            // Retrieve the product DTO by ID from the repository
            ProductDTOGet productDTO = productRepository.findProductDTOGetById(productId);
            // Check if the product DTO exists
            if (productDTO == null) {
                throw new ResourceNotFoundException("Product not found with ID: " + productId);
            }
            return productDTO;
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Product by ID", ex);
        }
    }

    // Method to fetch a product by Winery ID
    @Override
    public List<ProductDTOGet> getProductsByWineryId(Integer wineryId) throws ResourceNotFoundException {
        try {
            // Retrieve products associated with the Winery ID using the query method
            List<ProductDTOGet> productDTOs = productRepository.findProductsByWineryId(wineryId);
            // Check if any products are returned
            if (productDTOs.isEmpty()) {
                throw new ResourceNotFoundException("No products found for Winery ID: " + wineryId);
            }
            return productDTOs;
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while getting products by Winery ID", ex);
        }
    }

    // Method to fetch a product by Variety ID
    @Override
    public List<ProductDTOGet> getProductsByVarietyId(Integer varietyId) throws ResourceNotFoundException {
        try {
            // Retrieve products associated with the Variety ID using the query method
            List<ProductDTOGet> productDTOs = productRepository.findProductsByVarietyId(varietyId);
            // Check if any products are returned
            if (productDTOs.isEmpty()) {
                throw new ResourceNotFoundException("No products found for Variety ID: " + varietyId);
            }
            return productDTOs;
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while getting products by Variety ID", ex);
        }
    }

    // Method to fetch a product by Type ID
    @Override
    public List<ProductDTOGet> getProductsByTypeId(Integer typeId) throws ResourceNotFoundException {
        try {
            // Retrieve products associated with the Type ID using the query method
            List<ProductDTOGet> productDTOs = productRepository.findProductsByTypeId(typeId);
            // Check if any products are returned
            if (productDTOs.isEmpty()) {
                throw new ResourceNotFoundException("No products found for Type ID: " + typeId);
            }
            return productDTOs;
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while getting products by Type ID", ex);
        }
    }

    // Method to fetch random products
    @Override
    public List<ProductDTOGet> findRandomProducts() throws ResourceNotFoundException {
        try {
            // Create a Pageable object with a random sorting order and limit the result to 8 items
            Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"));
            // Retrieve random products from the repository using the query method
            Page<ProductDTOGet> page = productRepository.findRandProductDTOs(pageable);
            // Extract the content from the page
            List<ProductDTOGet> randomProductDTOs = page.getContent();
            // Check if any random products are found
            if (randomProductDTOs.isEmpty()) {
                throw new ResourceNotFoundException("No random products found in the database.");
            }
            return randomProductDTOs;
        } catch (Exception ex) {
            // If an exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while fetching random products", ex);
        }
    }

    // Method to create a new product
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        // Convert the ProductDTO to a Product entity
        Product productToStore = convertProductDTOToProduct(productDTO);
        try {
            // Save the product in the repository
            Product storedProduct = productRepository.save(productToStore);
            // Convert the stored Product entity to a DTO and return
            return convertProductToProductDTO(storedProduct);
        } catch (Exception e) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update an existing product
    @Override
    public ProductDTO updateProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        try {
            // Check if the product exists
            Product existingProduct = productRepository.findById(productDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productDTO.getId()));

            // Convert the ProductDTO to a Product entity
            Product updatedProduct = convertProductDTOToProduct(productDTO);

            // Update fields of the existing product
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setImage(updatedProduct.getImage());
            existingProduct.setYear(updatedProduct.getYear());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setStock(updatedProduct.getStock());
            existingProduct.setWinery(updatedProduct.getWinery());
            existingProduct.setVariety(updatedProduct.getVariety());
            existingProduct.setType(updatedProduct.getType());

            // Save the updated product
            Product savedProduct = productRepository.save(existingProduct);

            // Convert the updated product back to DTO and return
            return convertProductToProductDTO(savedProduct);
        } catch (BadRequestException | ResourceNotFoundException ex) {
            // If a BadRequestException or ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Product", ex);
        }
    }

    // Method to delete a product
    @Override
    public void deleteProduct(Integer productId) throws ResourceNotFoundException {
        try {
            // Check if the product exists
            Product existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
            // Delete the product
            productRepository.delete(existingProduct);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Product", ex);
        }
    }

    // Method to convert a ProductDTO to a Product entity
    @Override
    public Product convertProductDTOToProduct(ProductDTO productDTO) throws BadRequestException,
            ResourceNotFoundException {
        try {
            // Check if any required fields are null
            if (Stream.of(
                            productDTO.getId(), productDTO.getName(), productDTO.getDescription(), productDTO.getImage(),
                            productDTO.getYear(), productDTO.getPrice(), productDTO.getStock(),
                            productDTO.getIdWinery(), productDTO.getIdVariety(), productDTO.getIdType())
                    .anyMatch(Objects::isNull)) {
                // If any required field is null, throw a BadRequestException
                throw new BadRequestException("The received request does not have the correct format.");
            }

            // Create a new Product entity and set its fields
            Product product = new Product();
            product.setId(productDTO.getId());
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setImage(productDTO.getImage());
            product.setYear(productDTO.getYear());
            product.setPrice(productDTO.getPrice());
            product.setStock(productDTO.getStock());

            // Retrieve and set associated Winery, Variety, and Type entities
            Winery winery = wineryRepository.findById(productDTO.getIdWinery())
                    .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + productDTO.getIdWinery()));
            product.setWinery(winery);

            Variety variety = varietyRepository.findById(productDTO.getIdVariety())
                    .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + productDTO.getIdVariety()));
            product.setVariety(variety);

            Type type = typeRepository.findById(productDTO.getIdType())
                    .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + productDTO.getIdType()));
            product.setType(type);

            return product;
        } catch (BadRequestException | ResourceNotFoundException ex) {
            // If a BadRequestException or ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting ProductDTO to Product", ex);
        }
    }

    // Method to convert a Product entity to a ProductDTO
    @Override
    public ProductDTO convertProductToProductDTO(Product product) {
        try {
            // Create a new ProductDTO and set its fields
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setImage(product.getImage());
            productDTO.setYear(product.getYear());
            productDTO.setPrice(product.getPrice());
            productDTO.setStock(product.getStock());
            productDTO.setIdWinery(product.getWinery().getId());
            productDTO.setIdVariety(product.getVariety().getId());
            productDTO.setIdType(product.getType().getId());
            return productDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Product to ProductDTO", ex);
        }
    }
}


