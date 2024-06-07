package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.*;
import com.mscommerce.models.DTO.product.ProductDTO;
import com.mscommerce.models.DTO.product.ProductDTOGet;
import com.mscommerce.repositories.jpa.ProductRepository;
import com.mscommerce.repositories.jpa.TypeRepository;
import com.mscommerce.repositories.jpa.VarietyRepository;
import com.mscommerce.repositories.jpa.WineryRepository;
import com.mscommerce.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;

    private final WineryRepository wineryRepository;

    private final VarietyRepository varietyRepository;

    private final TypeRepository typeRepository;

    /**
     * Fetches all products with stock less than the specified amount.
     * @param stock The stock amount to compare with.
     * @return List of products with stock less than the specified amount.
     */
    @Transactional
    public List<Map<String, Object>> getAllByStockLessThan(Integer stock) {
        List<Product> products = productRepository.findAllByStockLessThan(stock);
        return products.stream().map(product -> {
            Map<String, Object> productData = new HashMap<>();
            Hibernate.initialize(product.getWinery());
            Hibernate.initialize(product.getVariety());
            Hibernate.initialize(product.getType());
            productData.put("Product ID", product.getId());
            productData.put("Product Name", product.getName());
            productData.put("Stock", product.getStock());
            return productData;
        }).collect(Collectors.toList());
    }

    /**
     * Fetches the top 10 products based on the sum of their stock and the count of their occurrences in order details.
     * The products are ordered first by the sum of their stock in descending order and then by the count of their occurrences in descending order.
     * @return List of object arrays, where each array contains a product, the count of its occurrences in order details, and the sum of its stock.
     */
    @Transactional
    public List<Map<String, Object>> getTop10ByOrderDetailsCountAndSumStock() {
        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> results = productRepository.findTop10ByOrderDetailsCountAndSumStock(topTen);
        return results.stream().map(result -> {
            Map<String, Object> productData = new HashMap<>();
            Product product = (Product) result[0];
            Long orderDetailsCount = (Long) result[1];
            Long sumStock = (Long) result[2];
            productData.put("Product ID", product.getId());
            productData.put("Product Name", product.getName());
            productData.put("Order Details Count", orderDetailsCount);
            productData.put("Units sold", sumStock);
            return productData;
        }).collect(Collectors.toList());
    }

    /**
     * Fetches the count of a specific product type and the sum of their stock in order details.
     * @param typeId ID of the product type.
     * @return List of object arrays, where each array contains a product type, the count of its occurrences in order details, and the sum of its stock.
     */
    @Transactional
    public List<Map<String, Object>> getCountByProductTypeAndSumStock(Integer typeId) {
        List<Object[]> results = productRepository.countByProductTypeAndSumStock(typeId);
        return results.stream().map(result -> {
            Map<String, Object> productData = new HashMap<>();
            Type type = (Type) result[0];
            Long orderDetailsCount = (Long) result[1];
            Long sumStock = (Long) result[2];
            productData.put("Product Type", type.getName());
            productData.put("Order Details Count", orderDetailsCount);
            productData.put("Units sold", sumStock);
            return productData;
        }).collect(Collectors.toList());
    }

    /**
     * Fetches all products.
     * @return List of all products.
     */
    @Override
    public List<ProductDTOGet> getAllProducts() throws ResourceNotFoundException {
        // Fetch all products from the repository and return them
        return productRepository.findAllProductDTOGet();
    }

    /**
     * Fetches a product by ID.
     * @param productId ID of the product.
     * @return The fetched product.
     */
    @Override
    public ProductDTOGet getProductById(Integer productId) throws ResourceNotFoundException {
        // Fetch the product DTO by ID from the repository
        ProductDTOGet productDTO = productRepository.findProductDTOGetById(productId);

        // Check if the product DTO exists
        if (productDTO == null) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }
        return productDTO;
    }

    /**
     * Fetches products by winery ID.
     * @param wineryId ID of the winery.
     * @return List of products.
     */
    @Override
    public List<ProductDTOGet> getProductsByWineryId(Integer wineryId) throws ResourceNotFoundException {
        // Fetch products associated with the Winery ID using the query method
        List<ProductDTOGet> productDTOs = productRepository.findProductsByWineryId(wineryId);

        // Check if any products are returned
        if (productDTOs.isEmpty()) {
            throw new ResourceNotFoundException("No products found for Winery ID: " + wineryId);
        }
        return productDTOs;
    }

    /**
     * Fetches products by variety ID.
     * @param varietyId ID of the variety.
     * @return List of products.
     */
    @Override
    public List<ProductDTOGet> getProductsByVarietyId(Integer varietyId) throws ResourceNotFoundException {
        // Fetch products associated with the Variety ID using the query method
        List<ProductDTOGet> productDTOs = productRepository.findProductsByVarietyId(varietyId);

        // Check if any products are returned
        if (productDTOs.isEmpty()) {
            throw new ResourceNotFoundException("No products found for Variety ID: " + varietyId);
        }
        return productDTOs;
    }

    /**
     * Fetches products by type ID.
     * @param typeId ID of the type.
     * @return List of products.
     */
    @Override
    public List<ProductDTOGet> getProductsByTypeId(Integer typeId) throws ResourceNotFoundException {
        // Fetch products associated with the Type ID using the query method
        List<ProductDTOGet> productDTOs = productRepository.findProductsByTypeId(typeId);

        // Check if any products are returned
        if (productDTOs.isEmpty()) {
            throw new ResourceNotFoundException("No products found for Type ID: " + typeId);
        }
        return productDTOs;
    }

    /**
     * Fetches random products.
     * @return List of random products.
     */
    @Override
    public List<ProductDTOGet> findRandomProducts() throws ResourceNotFoundException {
        // Create a Pageable object with a random sorting order and limit the result to 8 items
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"));

        // Fetch random products from the repository using the query method
        Page<ProductDTOGet> page = productRepository.findRandProductDTOs(pageable);

        // Extract the content from the page
        List<ProductDTOGet> randomProductDTOs = page.getContent();

        // Check if any random products are found
        if (randomProductDTOs.isEmpty()) {
            throw new ResourceNotFoundException("No random products found in the database.");
        }
        return randomProductDTOs;
    }

    /**
     * Creates a new product.
     * @param productDTO Contains the new product details.
     * @return The created product.
     */
    @Override
    public ProductDTO createProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateProductDTO(productDTO);

        // Convert the ProductDTO to a Product entity
        Product productToStore = convertProductDTOToProduct(productDTO);

        // Save the product in the repository
        Product storedProduct = productRepository.save(productToStore);

        // Convert the stored Product entity to a DTO and return
        return convertProductToProductDTO(storedProduct);
    }

    /**
     * Updates an existing product.
     * @param productDTO Contains the updated product details.
     * @return The updated product.
     */
    @Override
    public ProductDTO updateProduct(ProductDTO productDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateProductDTO(productDTO);

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
    }

    /**
     * Deletes a specific product.
     * @param productId ID of the product to be deleted.
     */
    @Override
    public void deleteProduct(Integer productId) throws ResourceNotFoundException {
        // Check if the product exists
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Delete the product
        productRepository.delete(existingProduct);
    }

    /**
     * Converts a DTO to a Product entity.
     * @param productDTO DTO to be converted.
     * @return Converted Product entity.
     */
    private Product convertProductDTOToProduct(ProductDTO productDTO) throws ResourceNotFoundException {
            // Create a new Product entity and set its fields
            Product product = new Product();
            product.setId(productDTO.getId());
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setImage(productDTO.getImage());
            product.setYear(productDTO.getYear());
            product.setPrice(productDTO.getPrice());
            product.setStock(productDTO.getStock());

            // Fetch and set associated Winery, Variety, and Type entities
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
    }

    /**
     * Converts a Product to a DTO.
     * @param product Product to be converted.
     * @return Converted DTO.
     */
    private ProductDTO convertProductToProductDTO(Product product) {
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
    }

    // Validation methods
    /**
     * Validates a ProductDTO.
     * @param productDTO DTO to be validated.
     */
    private void validateProductDTO(ProductDTO productDTO) throws BadRequestException {
        if (productDTO.getId() == null) {
            throw new BadRequestException("Product ID must not be null");
        }
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Product name must not be null or empty");
        }
        if (productDTO.getDescription() == null || productDTO.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Product description must not be null or empty");
        }
        if (productDTO.getImage() == null || productDTO.getImage().trim().isEmpty()) {
            throw new BadRequestException("Product image must not be null or empty");
        }
        if (productDTO.getYear() == null || productDTO.getYear() < 0) {
            throw new BadRequestException("Product year must not be null and must be a positive number");
        }
        if (productDTO.getPrice() == null || productDTO.getPrice() < 0) {
            throw new BadRequestException("Product price must not be null and must be a positive number");
        }
        if (productDTO.getStock() == null || productDTO.getStock() < 0) {
            throw new BadRequestException("Product stock must not be null and must be a positive number");
        }
        if (productDTO.getIdWinery() == null) {
            throw new BadRequestException("Winery ID must not be null");
        }
        if (productDTO.getIdVariety() == null) {
            throw new BadRequestException("Variety ID must not be null");
        }
        if (productDTO.getIdType() == null) {
            throw new BadRequestException("Type ID must not be null");
        }
    }
}


