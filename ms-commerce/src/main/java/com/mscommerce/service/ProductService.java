package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.ProductDTO;
import com.mscommerce.models.Product;
import com.mscommerce.models.Type;
import com.mscommerce.models.Variety;
import com.mscommerce.models.Winery;
import com.mscommerce.repositories.ProductRepository;
import com.mscommerce.repositories.TypeRepository;
import com.mscommerce.repositories.VarietyRepository;
import com.mscommerce.repositories.WineryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final WineryRepository wineryRepository;

    private final VarietyRepository varietyRepository;

    private final TypeRepository typeRepository;

    // Method to fetch all products
    public List<ProductDTO> getAllProducts() throws ResourceNotFoundException {
        try {
            // Retrieve all products from the repository
            List<Product> products = productRepository.findAll();

            // Convert the list of Product entities to a list of DTOs
            return products.stream()
                    .map(this::convertProductToProductDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch products");
        }
    }

    // Method to fetch a product by ID
    public ProductDTO getProductById(Integer productId) throws ResourceNotFoundException {
        try {
            // Retrieve the product by ID from the repository
            Optional<Product> productOptional = productRepository.findById(productId);

            // Check if the product exists
            if (productOptional.isEmpty()) {
                throw new ResourceNotFoundException("Product not found with ID: " + productId);
            }

            // Convert the retrieved Product entity to a DTO
            Product product = productOptional.get();
            return convertProductToProductDTO(product);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Product by ID", ex);
        }
    }

    // Method to fetch products by Winery ID
    public List<ProductDTO> getProductsByWineryId(Integer wineryId) throws ResourceNotFoundException {
        // Retrieve the Winery by ID from the repository
        Winery winery = wineryRepository.findById(wineryId)
                .orElseThrow(() -> new ResourceNotFoundException("Winery not found with ID: " + wineryId));

        // Retrieve products associated with the Winery ID
        List<Product> products = productRepository.findByWineryId(wineryId);

        // Convert the list of Product entities to a list of DTOs
        return products.stream()
                .map(this::convertProductToProductDTO)
                .collect(Collectors.toList());
    }

    // Method to fetch products by Variety ID
    public List<ProductDTO> getProductsByVarietyId(Integer varietyId) throws ResourceNotFoundException {
        // Retrieve the Variety by ID from the repository
        Variety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Variety not found with ID: " + varietyId));

        // Retrieve products associated with the Variety ID
        List<Product> products = productRepository.findByVarietyId(varietyId);

        // Convert the list of Product entities to a list of DTOs
        return products.stream()
                .map(this::convertProductToProductDTO)
                .collect(Collectors.toList());
    }

    // Method to fetch products by Type ID
    public List<ProductDTO> getProductsByTypeId(Integer typeId) throws ResourceNotFoundException {
        // Retrieve the Type by ID from the repository
        Type type = typeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("Type not found with ID: " + typeId));

        // Retrieve products associated with the Type ID
        List<Product> products = productRepository.findByTypeId(typeId);

        // Convert the list of Product entities to a list of DTOs
        return products.stream()
                .map(this::convertProductToProductDTO)
                .collect(Collectors.toList());
    }

    // Method to fetch random products
    public List<ProductDTO> findRandomProducts() throws ResourceNotFoundException {
        // Retrieve random products from the repository
        List<Product> randomProducts = productRepository.findRandProducts();

        // Check if any random products are found
        if (randomProducts.isEmpty()) {
            throw new ResourceNotFoundException("No random products found in the database.");
        }

        // Convert the list of Product entities to a list of DTOs
        return randomProducts.stream()
                .map(this::convertProductToProductDTO)
                .collect(Collectors.toList());
    }

    // Method to create a new product
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
    private Product convertProductDTOToProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
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
    private ProductDTO convertProductToProductDTO(Product product) {
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


