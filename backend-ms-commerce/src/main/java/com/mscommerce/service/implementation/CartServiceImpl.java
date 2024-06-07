package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.*;
import com.mscommerce.models.DTO.cart.CartDTO;
import com.mscommerce.models.DTO.cart.CartDTORequest;
import com.mscommerce.models.DTO.cart.CartDTOUpdate;
import com.mscommerce.repositories.jpa.CartRepository;
import com.mscommerce.repositories.jpa.ProductRepository;
import com.mscommerce.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final Integer cartLimit = 10;

    /**
     * Fetches all carts for administrative viewing.
     * @return List of all carts.
     */
    @Override
    public List<CartDTO> adminGetAllCarts() {
        // Fetch all carts from the repository
        List<Cart> cartList = cartRepository.findAll();

        // Convert the list of Cart entities to a list of DTOs
        return cartList.stream()
                .map(this::convertCartToCartDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all carts for the current user.
     * @return List of user's carts.
     */
    @Override
    public List<CartDTO> getAllCarts() {
        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch all carts for the logged-in user
        List<Cart> carts = cartRepository.findByIdCustomer(idCustomer);

        // Convert the list of Cart entities to a list of DTOs
        return carts.stream()
                .map(this::convertCartToCartDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a specific cart by ID for administrative viewing.
     * @param cartId ID of the cart to be fetched.
     * @return The fetched cart.
     */
    @Override
    public CartDTO adminGetCartById(Integer cartId) throws ResourceNotFoundException {
        // Fetch the cart by ID from the repository
        Cart cart = getCartById(cartId);

        // Convert the retrieved Cart entity to a DTO
        return convertCartToCartDTO(cart);
    }

    /**
     * Creates a new cart for administrators.
     * @param cartDTO Contains the new cart details.
     * @return The created cart.
     */
    @Override
    public CartDTO adminCreateCart(CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException, InsufficientStockException {
        // Validate the input DTO
        validateCartDTO(cartDTO);

        // Fetch the product by ID from the repository
        Product product = productRepository.findById(cartDTO.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartDTO.getIdProduct()));

        // Check if there is enough stock
        if (cartDTO.getQuantity() > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Convert the DTO to a Cart entity
        Cart cartToStore = convertCartDTOToCart(cartDTO);

        // Save the Cart entity to the repository
        Cart savedCart = cartRepository.save(cartToStore);

        // Set the ID of the DTO to the ID of the saved Cart entity
        cartDTO.setId(savedCart.getId());
        return cartDTO;
    }

    /**
     * Creates a new cart for the current user.
     * @param cartDTORequest Contains the new cart details.
     * @return The created cart.
     */
    @Override
    @Transactional
    public CartDTO createCart(CartDTORequest cartDTORequest) throws BadRequestException, ResourceNotFoundException, InsufficientStockException {
        // Validate the input DTO
        validateCartDTORequest(cartDTORequest);

        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Check if the customer already has 10 products in their cart
        Long productCount = cartRepository.countByIdCustomer(idCustomer);
        if (productCount >= cartLimit) {
            throw new BadRequestException("You can't add another item to your cart because you reached your limit of " + cartLimit + " items.");
        }

        // Fetch all the existing Cart entities associated with the user
        List<Cart> existingCarts = cartRepository.findByIdCustomer(idCustomer);

        // Calculate the total quantity for the product in the existing carts
        int totalQuantity = existingCarts.stream()
                .filter(cart -> cart.getProduct().getId().equals(cartDTORequest.getIdProduct()))
                .mapToInt(Cart::getQuantity)
                .sum();

        // Add the quantity from the CartDTORequest to the total quantity
        totalQuantity += cartDTORequest.getQuantity();

        // Fetch the product by ID from the repository
        Product product = productRepository.findById(cartDTORequest.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartDTORequest.getIdProduct()));

        // Check if there is enough stock
        if (totalQuantity > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Create a Cart entity from OrderDTORequest
        Cart cart = new Cart();
        cart.setIdCustomer(idCustomer);
        cart.setProduct(product);
        cart.setPrice(product.getPrice());
        cart.setQuantity(cartDTORequest.getQuantity());

        // Save the Cart entity
        Cart savedCart = cartRepository.save(cart);

        // Return the saved Cart entity as a DTO
        return convertCartToCartDTO(savedCart);
    }

    /**
     * Updates an existing cart. Intended for administrators.
     * @param cartDTO Contains the updated cart details.
     * @return Updated cart.
     */
    @Override
    @Transactional
    public CartDTO adminUpdateCart(CartDTO cartDTO) throws BadRequestException, ResourceNotFoundException, InsufficientStockException {
        // Validate the input DTO
        validateCartDTO(cartDTO);

        // Fetch the Cart entity by ID from the repository
        Cart cart = getCartById(cartDTO.getId());

        // Fetch the product by ID from the repository
        Product product = productRepository.findById(cartDTO.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartDTO.getIdProduct()));

        // Check if there is enough stock
        if (cartDTO.getQuantity() > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Update the idCustomer, idProduct, price, and quantity
        cart.setIdCustomer(cartDTO.getIdCustomer());
        cart.setProduct(product);
        cart.setPrice(cartDTO.getPrice());
        cart.setQuantity(cartDTO.getQuantity());

        // Save the updated Cart entity
        Cart updatedCart = cartRepository.save(cart);

        // Return the updated Cart entity as a DTO
        return convertCartToCartDTO(updatedCart);
    }

    /**
     * Updates the quantity of a cart item for the current user.
     * @param cartDTOUpdate Contains the cart item ID and new quantity.
     * @return Updated cart item.
     */
    @Override
    @Transactional
    public CartDTO updateCart(CartDTOUpdate cartDTOUpdate) throws BadRequestException, ResourceNotFoundException, InsufficientStockException {
        // Validate the input DTO
        validateCartDTOUpdate(cartDTOUpdate);

        // Fetch the Cart entity by ID from the repository
        Cart cart = getCartById(cartDTOUpdate.getId());

        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch all the existing Cart entities associated with the user
        List<Cart> existingCarts = cartRepository.findByIdCustomer(idCustomer);

        // Calculate the total quantity for the product in the existing carts
        int totalQuantity = existingCarts.stream()
                .filter(existingCart -> existingCart.getProduct().getId().equals(cart.getProduct().getId()))
                .mapToInt(Cart::getQuantity)
                .sum();

        // Subtract the existing quantity of the cart item from the total quantity
        totalQuantity -= cart.getQuantity();

        // Add the new quantity from the CartDTOUpdate to the total quantity
        totalQuantity += cartDTOUpdate.getQuantity();

        // Fetch the product by ID from the repository
        Product product = productRepository.findById(cart.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cart.getProduct().getId()));

        // Check if there is enough stock
        if (totalQuantity > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Update the quantity
        cart.setQuantity(cartDTOUpdate.getQuantity());

        // Save the updated Cart entity
        Cart updatedCart = cartRepository.save(cart);

        // Return the updated Cart entity as a DTO
        return convertCartToCartDTO(updatedCart);
    }


    /**
     * Deletes a specific cart. Intended for administrators.
     * @param cartId ID of the cart to be deleted.
     */
    @Override
    @Transactional
    public void adminDeleteCart(Integer cartId) throws ResourceNotFoundException {
        // Fetch the cart by ID from the repository
        Cart cart = getCartById(cartId);

        // Delete the cart from the repository
        cartRepository.delete(cart);
    }

    /**
     * Deletes a specific cart item for the current user.
     * @param cartId ID of the cart item to be deleted.
     */
    @Override
    @Transactional
    public void deleteCart(Integer cartId) throws ResourceNotFoundException {
        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch the cart by ID from the repository
        Cart cart = getCartById(cartId);

        // Check if the cart belongs to the logged-in user
        if (!cart.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("You do not have permission to delete this cart.");
        }

        // Delete the cart from the repository
        cartRepository.delete(cart);
    }

    //Empties the cart for the current user.
    @Override
    @Transactional
    public void cleanCart() {
        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch all the existing Cart entities associated with the user
        List<Cart> existingCarts = cartRepository.findByIdCustomer(idCustomer);

        // Loop through the existing Cart entities and delete each one
        for (Cart existingCart : existingCarts) {
            cartRepository.delete(existingCart);
        }
    }

    /**
     * Converts a CartDTO object to a Cart entity.
     * @param cartDTO A CartDTO object to be converted.
     * @return A Cart entity.
     */
    public Cart convertCartDTOToCart(CartDTO cartDTO) throws ResourceNotFoundException {
        // Create a new Cart entity and set its fields
        Cart cart = new Cart();
        cart.setId(cartDTO.getId());
        cart.setIdCustomer(cartDTO.getIdCustomer());
        cart.setPrice(cartDTO.getPrice());
        cart.setQuantity(cartDTO.getQuantity());

        // Fetch and set associated product entity
        Product product = productRepository.findById(cartDTO.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartDTO.getIdProduct()));
        cart.setProduct(product);
        return cart;
    }

    /**
     * Converts a Cart entity to a CartDTO object.
     * @param cart A Cart entity to be converted.
     * @return A CartDTO object.
     */
    public CartDTO convertCartToCartDTO(Cart cart) {
        // Create a new CartDTO and set its fields
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setIdCustomer(cart.getIdCustomer());
        cartDTO.setIdProduct(cart.getProduct().getId());
        cartDTO.setPrice(cart.getPrice());
        cartDTO.setQuantity(cart.getQuantity());
        return cartDTO;
    }

    private Cart getCartById(Integer cartId) throws ResourceNotFoundException {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));
    }

    /**
     * Validates a CartDTO object.
     * @param cartDTO A CartDTO object to be validated.
     */
    private void validateCartDTO(CartDTO cartDTO) throws BadRequestException {
        if (cartDTO.getIdCustomer() == null || cartDTO.getIdCustomer().trim().isEmpty()) {
            throw new BadRequestException("Customer ID must not be null or empty");
        }
        if (cartDTO.getIdProduct() == null) {
            throw new BadRequestException("Product ID must not be null");
        }
        if (cartDTO.getPrice() == null || cartDTO.getPrice() < 0) {
            throw new BadRequestException("Price must not be null and must be greater than or equal to 0");
        }
        if (cartDTO.getQuantity() == null || cartDTO.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }

    /**
     * Validates a CartDTORequest object.
     * @param cartDTORequest A CartDTORequest object to be validated.
     */
    private void validateCartDTORequest(CartDTORequest cartDTORequest) throws BadRequestException {
        if (cartDTORequest.getIdProduct() == null) {
            throw new BadRequestException("Product ID must not be null");
        }
        if (cartDTORequest.getQuantity() == null || cartDTORequest.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }

    /**
     * Validates a CartDTOUpdate object.
     * @param cartDTOUpdate A CartDTOUpdate object to be validated.
     */
    private void validateCartDTOUpdate(CartDTOUpdate cartDTOUpdate) throws BadRequestException {
        if (cartDTOUpdate.getId() == null) {
            throw new BadRequestException("Cart ID must not be null");
        }
        if (cartDTOUpdate.getQuantity() == null || cartDTOUpdate.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }

}
