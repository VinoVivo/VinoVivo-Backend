package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTO;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTOUpdate;
import com.mscommerce.models.Order;
import com.mscommerce.models.OrderDetails;
import com.mscommerce.models.Product;
import com.mscommerce.repositories.jpa.OrderDetailsRepository;
import com.mscommerce.repositories.jpa.OrderRepository;
import com.mscommerce.repositories.jpa.ProductRepository;
import com.mscommerce.service.IOrderDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailsServiceImpl implements IOrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final OrderServiceImpl orderService;

    /**
     * Fetches all order details for administrators.
     * @return List of all order details.
     */
    @Override
    public List<OrderDetailsDTO> adminGetAllOrderDetails() throws ResourceNotFoundException {
        // Fetch all order details from the repository
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findAll();

        // Convert the list of OrderDetails entities to a list of DTOs
        return orderDetailsList.stream()
                .map(this::convertOrderDetailsToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all order details for the current user.
     * @return List of user's order details.
     */
    @Override
    public List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException {
        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch all orders for the logged-in user
        List<Order> userOrders = orderRepository.findByIdCustomer(idCustomer);

        if (userOrders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for the logged-in user");
        }

        // Fetch all order details for the user's orders
        List<Integer> userOrderIds = userOrders.stream().map(Order::getId).collect(Collectors.toList());
        List<OrderDetails> userOrderDetails = orderDetailsRepository.findByIdOrderIn(userOrderIds);

        // Convert the list of OrderDetails entities to a list of DTOs
        return userOrderDetails.stream()
                .map(this::convertOrderDetailsToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches order details for a specific order of the current user.
     * @param orderId ID of the order.
     * @return List of order details for the specified order.
     */
    @Override
    public List<OrderDetailsDTO> getOrderDetailsByOrderId(Integer orderId) throws ResourceNotFoundException {
        // Get the user ID from Keycloak Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch the order by orderId
        Order order = orderService.getOrderById(orderId);

        // Check if the order belongs to the logged-in user
        if (!order.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("You are not authorized to view the details of this order");
        }

        // Fetch all order details for the order
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(orderId);

        // Convert the list of OrderDetails entities to a list of DTOs
        return orderDetailsList.stream()
                .map(this::convertOrderDetailsToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Fetches a specific order detail by ID for administrators.
     * @param orderDetailsId ID of the order detail.
     * @return The fetched order detail.
     */
    @Override
    public OrderDetailsDTO adminGetOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException {
        // Fetch the order details by ID from the repository
        OrderDetails orderDetails = getOrderDetailsById(orderDetailsId);

        // Convert the retrieved OrderDetails entity to a DTO
        return convertOrderDetailsToDTO(orderDetails);
    }

    /**
     * Creates a new order detail for administrators.
     * @param orderDetailsDTO Contains the new order detail details.
     * @return The created order detail.
     */
    @Override
    @Transactional
    public OrderDetailsDTO adminCreateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateOrderDetailsDTO(orderDetailsDTO);

        // Convert the DTO to an OrderDetails entity
        OrderDetails orderDetailsToStore = convertDTOToOrderDetails(orderDetailsDTO);

        // Fetch the associated product
        Product product = orderDetailsToStore.getProduct();

        // Check if there is enough stock for the requested quantity
        if (orderDetailsDTO.getQuantity() > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Save the OrderDetails entity to the repository
        OrderDetails savedOrderDetails = orderDetailsRepository.save(orderDetailsToStore);

        // Update the product stock
        product.setStock(product.getStock() - orderDetailsDTO.getQuantity());

        // Save the updated product to the repository
        productRepository.save(product);

        // Set the generated ID in the DTO
        orderDetailsDTO.setId(savedOrderDetails.getId());

        // Return the DTO with the generated ID
        return orderDetailsDTO;
    }

    /**
     * Creates a new order detail for an existing order of the current user.
     * @param orderDetailsDTOAddition Contains the new order detail details.
     * @return The created order detail.
     */
    @Override
    @Transactional
    public OrderDetailsDTO createOrderDetailsToExistingOrder(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateOrderDetailsDTOAddition(orderDetailsDTOAddition);

        // Get the user ID from the authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch the order by ID from the repository
        Order order = orderService.getOrderById(orderDetailsDTOAddition.getIdOrder());

        // Check if the order belongs to the logged-in user
        if (!order.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("You are not authorized to add details to this order");
        }

        // Fetch the product by ID from the repository
        Product product = productRepository.findById(orderDetailsDTOAddition.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderDetailsDTOAddition.getIdProduct()));

        // Calculate the price per unit and the total price
        Double pricePerUnit = product.getPrice();
        Double totalPrice = pricePerUnit * orderDetailsDTOAddition.getQuantity();

        // Check if there is enough stock for the requested quantity
        if (orderDetailsDTOAddition.getQuantity() > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Calculate the updated total price of the order
        Double updatedTotalPrice = order.getTotalPrice() + totalPrice;

        // Update the total price of the order in the repository
        orderRepository.updateTotalPriceById(updatedTotalPrice, order.getId());

        // Create a new OrderDetails entity and set its fields
        OrderDetails orderDetailsToStore = new OrderDetails();
        orderDetailsToStore.setIdOrder(order.getId());
        orderDetailsToStore.setProduct(product);
        orderDetailsToStore.setPrice(pricePerUnit);
        orderDetailsToStore.setQuantity(orderDetailsDTOAddition.getQuantity());

        // Save the OrderDetails entity to the repository
        OrderDetails savedOrderDetails = orderDetailsRepository.save(orderDetailsToStore);

        // Update the product stock
        product.setStock(product.getStock() - orderDetailsDTOAddition.getQuantity());

        // Save the updated product to the repository
        productRepository.save(product);

        // Return the saved OrderDetails entity as a DTO
        return new OrderDetailsDTO(
                savedOrderDetails.getId(),
                savedOrderDetails.getIdOrder(),
                savedOrderDetails.getProduct().getId(),
                savedOrderDetails.getPrice(),
                savedOrderDetails.getQuantity()
        );
    }

    /**
     * Updates an existing order detail for administrators.
     * @param orderDetailsDTO Contains the updated order detail details.
     * @return The updated order detail.
     */
    @Override
    @Transactional
    public OrderDetailsDTO adminUpdateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateOrderDetailsDTO(orderDetailsDTO);

        // Fetch the existing order details by ID from the repository
        OrderDetails existingOrderDetails = getOrderDetailsById(orderDetailsDTO.getId());

        // Fetch the associated order
        Order existingOrder = orderService.getOrderById(existingOrderDetails.getIdOrder());

        // Calculate the old total price of the order and the old price of the order detail
        Double oldTotalPrice = existingOrder.getTotalPrice();
        Double oldOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();

        // Convert the DTO to an OrderDetails entity
        existingOrderDetails = convertDTOToOrderDetails(orderDetailsDTO);

        // Calculate the new price of the order detail
        Double newOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();

        // Calculate the price difference
        Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;

        // Fetch the product
        Product product = existingOrderDetails.getProduct();

        // Calculate the quantity difference
        int quantityDifference = existingOrderDetails.getQuantity() - orderDetailsDTO.getQuantity();

        // Calculate the new stock
        int newStock = product.getStock() + quantityDifference;

        // Check if there is enough stock
        if (newStock < 0) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName());
        }

        // Update the product stock
        product.setStock(newStock);

        // Save the updated product to the repository
        productRepository.save(product);

        // Save the updated order details to the repository
        OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

        // Calculate the updated total price of the order
        Double updatedTotalPrice = oldTotalPrice + priceDifference;

        // Update the total price of the order in the repository
        orderRepository.updateTotalPriceById(updatedTotalPrice, existingOrder.getId());

        // Convert the updated order details back to a DTO and return it
        return convertOrderDetailsToDTO(savedOrderDetails);
    }

    /**
     * Updates the quantity of an existing order detail of the current user.
     * @param orderDetailsDTOUpdate Contains the updated order detail details.
     * @return The updated order detail.
     */
    @Override
    @Transactional
    public OrderDetailsDTO updateOrderDetails(OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateOrderDetailsDTOUpdate(orderDetailsDTOUpdate);

        // Get the user ID from the authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch the existing order details by ID from the repository
        OrderDetails existingOrderDetails = getOrderDetailsById(orderDetailsDTOUpdate.getId());

        // Fetch the associated order
        Order existingOrder = orderService.getOrderById(existingOrderDetails.getIdOrder());

        // Check if the order belongs to the logged-in user
        if (!existingOrder.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("Order does not belong to the logged-in user");
        }

        // Check if the order ID matches the order details
        if (!existingOrderDetails.getIdOrder().equals(orderDetailsDTOUpdate.getIdOrder())) {
            throw new BadRequestException("Order ID does not match the order details");
        }

        // Fetch the associated product
        Product product = existingOrderDetails.getProduct();

        // Calculate the quantity difference
        int quantityDifference = existingOrderDetails.getQuantity() - orderDetailsDTOUpdate.getQuantity();

        // Calculate the new stock
        int newStock = product.getStock() + quantityDifference;

        // Check if there is enough stock
        if (newStock < 0) {
            throw new InsufficientStockException("Not enough stock for product: " + product.getName());
        }

        // Update the product stock
        product.setStock(newStock);

        // Save the updated product to the repository
        productRepository.save(product);

        // Update the quantity of the order details
        existingOrderDetails.setQuantity(orderDetailsDTOUpdate.getQuantity());

        // Save the updated order details to the repository
        OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

        // Calculate the old total price of the order and the old price of the order detail
        Double oldTotalPrice = existingOrder.getTotalPrice();
        Double oldOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();

        // Calculate the new price of the order detail
        Double newOrderDetailPrice = product.getPrice() * orderDetailsDTOUpdate.getQuantity();

        // Calculate the price difference
        Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;

        // Calculate the updated total price of the order
        Double updatedTotalPrice = oldTotalPrice + priceDifference;

        // Update the total price of the order in the repository
        orderRepository.updateTotalPriceById(updatedTotalPrice, existingOrder.getId());

        // Convert the updated order details back to a DTO and return it
        return convertOrderDetailsToDTO(savedOrderDetails);
    }

    /**
     * Deletes a specific order detail for administrators even if the order associated with it doesn't exist.
     * @param orderDetailsId ID of the order detail to be deleted.
     */
    @Override
    @Transactional
    public void adminForceDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException {
        // Fetch the existing order details by ID from the repository
        OrderDetails existingOrderDetails = getOrderDetailsById(orderDetailsId);

        // Fetch the ID of the associated order
        Integer orderId = existingOrderDetails.getIdOrder();

        // Fetch the associated product
        Product product = existingOrderDetails.getProduct();

        // Fetch the associated order
        Order order = orderRepository.findById(orderId).orElse(null);

        // If the product exists
        if (product != null) {
            // Update the product stock
            product.setStock(product.getStock() + existingOrderDetails.getQuantity());

            // Save the updated product to the repository
            productRepository.save(product);
        }

        // If the order exists
        if (order != null) {
            // Update the total price of the order
            order.setTotalPrice(order.getTotalPrice() - (existingOrderDetails.getPrice() * existingOrderDetails.getQuantity()));

            // Fetch all other order details associated with the order
            List<OrderDetails> otherOrderDetails = orderDetailsRepository.findAllByIdOrder(orderId);

            // If there are no other order details associated with the order
            if (otherOrderDetails.size() == 1) {
                // Delete the order details and the associated order
                orderDetailsRepository.delete(existingOrderDetails);
                orderRepository.delete(order);
                return;
            } else {
                // Save the updated order to the repository
                orderRepository.save(order);
            }
        }

        // Delete the order details
        orderDetailsRepository.delete(existingOrderDetails);
    }

    /**
     * Deletes a specific order detail for administrators.
     * @param orderDetailsId ID of the order detail to be deleted.
     */
    @Override
    @Transactional
    public void adminDeleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException {
        // Fetch the order details by ID from the repository
        OrderDetails existingOrderDetails = getOrderDetailsById(orderDetailsId);

        // Fetch the associated order
        Order associatedOrder = orderService.getOrderById(existingOrderDetails.getIdOrder());

        // Fetch the product
        Product product = existingOrderDetails.getProduct();

        // Update product stock
        product.setStock(product.getStock() + existingOrderDetails.getQuantity());
        productRepository.save(product);

        // Check if the associated order has any other order details
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findAllByIdOrder(associatedOrder.getId());
        if (orderDetailsList.size() == 1) {
            // If there are no other order details associated
            // Delete both the order details and the associated order without updating the total price
            orderDetailsRepository.delete(existingOrderDetails);
            orderRepository.delete(associatedOrder);
        } else {
            // Calculate the updated total price of the order
            Double updatedTotalPrice = associatedOrder.getTotalPrice() - (existingOrderDetails.getPrice() * existingOrderDetails.getQuantity());

            // Update the total price of the order using the custom query method
            orderRepository.updateTotalPriceById(updatedTotalPrice, associatedOrder.getId());

            // Delete the order details
            orderDetailsRepository.delete(existingOrderDetails);
        }
    }

    /**
     * Deletes a specific order detail of the current user.
     * @param orderDetailsId ID of the order detail to be deleted.
     */
    @Override
    @Transactional
    public void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException {
        // Get the user ID from the authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idCustomer = authentication.getName();

        // Fetch the existing order details by ID from the repository
        OrderDetails existingOrderDetails = getOrderDetailsById(orderDetailsId);

        // Fetch the associated order
        Order associatedOrder = orderService.getOrderById(existingOrderDetails.getIdOrder());

        // Check if the order belongs to the logged-in user
        if (!associatedOrder.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("You are not authorized to delete details of this order");
        }

        // Fetch the associated product
        Product product = existingOrderDetails.getProduct();

        // Update the product stock
        product.setStock(product.getStock() + existingOrderDetails.getQuantity());

        // Save the updated product to the repository
        productRepository.save(product);

        // Fetch all order details associated with the order
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findAllByIdOrder(associatedOrder.getId());

        // If there are no other order details associated with the order
        if (orderDetailsList.size() == 1) {
            // Delete the order details and the associated order
            orderDetailsRepository.delete(existingOrderDetails);
            orderRepository.delete(associatedOrder);
        } else {
            // Calculate the updated total price of the order
            Double updatedTotalPrice = associatedOrder.getTotalPrice() - (existingOrderDetails.getPrice() * existingOrderDetails.getQuantity());

            // Update the total price of the order in the repository
            orderRepository.updateTotalPriceById(updatedTotalPrice, associatedOrder.getId());

            // Delete the order details
            orderDetailsRepository.delete(existingOrderDetails);
        }
    }

    /**
     * Converts a DTO to an OrderDetails entity.
     * @param orderDetailsDTO DTO to be converted.
     * @return Converted OrderDetails entity.
     */
    private OrderDetails convertDTOToOrderDetails(OrderDetailsDTO orderDetailsDTO) throws ResourceNotFoundException {
        // Create a new OrderDetails entity and set its fields
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setId(orderDetailsDTO.getId());
        orderDetails.setIdOrder(orderDetailsDTO.getIdOrder());
        Product product = productRepository.findById(orderDetailsDTO.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetailsDTO.getIdProduct()));
        orderDetails.setProduct(product);
        orderDetails.setPrice(orderDetailsDTO.getPrice());
        orderDetails.setQuantity(orderDetailsDTO.getQuantity());
        return orderDetails;
    }

    /**
     * Converts an OrderDetails entity to a DTO.
     * @param orderDetails Entity to be converted.
     * @return Converted DTO.
     */
    private OrderDetailsDTO convertOrderDetailsToDTO(OrderDetails orderDetails) {
        // Create a new OrderDetailsDTO and set its fields
        OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();
        orderDetailsDTO.setId(orderDetails.getId());
        orderDetailsDTO.setIdOrder(orderDetails.getIdOrder());
        orderDetailsDTO.setIdProduct(orderDetails.getProduct().getId());
        orderDetailsDTO.setPrice(orderDetails.getPrice());
        orderDetailsDTO.setQuantity(orderDetails.getQuantity());
        return orderDetailsDTO;
    }

    /**
     * Fetches OrderDetails by ID.
     * @param orderDetailsId ID of the OrderDetails.
     * @return Fetched OrderDetails.
     */
    private OrderDetails getOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException {
        return orderDetailsRepository.findById(orderDetailsId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));
    }

    // Validation methods
    /**
     * Validates an OrderDetailsDTO.
     * @param orderDetailsDTO DTO to be validated.
     */
    private void validateOrderDetailsDTO(OrderDetailsDTO orderDetailsDTO) throws BadRequestException {
        if (orderDetailsDTO.getIdOrder() == null) {
            throw new BadRequestException("Order ID must not be null");
        }
        if (orderDetailsDTO.getIdProduct() == null) {
            throw new BadRequestException("Product ID must not be null");
        }
        if (orderDetailsDTO.getPrice() == null || orderDetailsDTO.getPrice() < 0) {
            throw new BadRequestException("Price must not be null and must be greater than or equal to 0");
        }
        if (orderDetailsDTO.getQuantity() == null || orderDetailsDTO.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }

    /**
     * Validates an OrderDetailsDTOAddition.
     * @param orderDetailsDTOAddition DTO to be validated.
     */
    private void validateOrderDetailsDTOAddition(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException {
        if (orderDetailsDTOAddition.getIdOrder() == null) {
            throw new BadRequestException("Order ID must not be null");
        }
        if (orderDetailsDTOAddition.getIdProduct() == null) {
            throw new BadRequestException("Product ID must not be null");
        }
        if (orderDetailsDTOAddition.getQuantity() == null || orderDetailsDTOAddition.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }

    /**
     * Validates an OrderDetailsDTOUpdate.
     * @param orderDetailsDTOUpdate DTO to be validated.
     */
    private void validateOrderDetailsDTOUpdate(OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException {
        if (orderDetailsDTOUpdate.getId() == null) {
            throw new BadRequestException("OrderDetails ID must not be null");
        }
        if (orderDetailsDTOUpdate.getIdOrder() == null) {
            throw new BadRequestException("Order ID must not be null");
        }
        if (orderDetailsDTOUpdate.getQuantity() == null || orderDetailsDTOUpdate.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must not be null and must be greater than 0");
        }
    }
}
