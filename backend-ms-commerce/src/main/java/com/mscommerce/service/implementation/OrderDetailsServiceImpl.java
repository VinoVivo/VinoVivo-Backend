package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.OrderDetailsDTO;
import com.mscommerce.models.DTO.OrderDetailsDTOAddition;
import com.mscommerce.models.DTO.OrderDetailsDTOUpdate;
import com.mscommerce.models.Order;
import com.mscommerce.models.OrderDetails;
import com.mscommerce.models.Product;
import com.mscommerce.repositories.OrderDetailsRepository;
import com.mscommerce.repositories.OrderRepository;
import com.mscommerce.repositories.ProductRepository;
import com.mscommerce.service.IOrderDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderDetailsServiceImpl implements IOrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // Method to fetch all order details
    @Override
    public List<OrderDetailsDTO> adminGetAllOrderDetails() throws ResourceNotFoundException {
        try {
            // Fetch all order details from the repository
            List<OrderDetails> orderDetailsList = orderDetailsRepository.findAll();

            // Convert the list of OrderDetails entities to a list of DTOs
            return orderDetailsList.stream()
                    .map(this::convertOrderDetailsToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch order details");
        }
    }

    @Override
    public List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException {
        try {
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
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch order details for the logged-in user");
        }
    }

    @Override
    public List<OrderDetailsDTO> getOrderDetailsByOrderId(Integer orderId) throws ResourceNotFoundException {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Fetch the order by orderId
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

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
        catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch order details for the specified order");
        }
    }

    // Method to fetch order details by ID
    @Override
    public OrderDetailsDTO adminGetOrderDetailsById(Integer orderDetailsId) {
        try {
            // Fetch the order details by ID from the repository
            OrderDetails orderDetails = orderDetailsRepository.findById(orderDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));

            // Convert the retrieved OrderDetails entity to a DTO
            return convertOrderDetailsToDTO(orderDetails);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting OrderDetails by ID", ex);
        }
    }

    // Admin method to create new order details
    @Override
    @Transactional
    public OrderDetailsDTO adminCreateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        // Convert the DTO to an OrderDetails entity
        OrderDetails orderDetailsToStore = convertDTOToOrderDetails(orderDetailsDTO);

        // Fetch the product from the database
        Product product = productRepository.findById(orderDetailsDTO.getIdProduct())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetailsDTO.getIdProduct()));

        // Check if there is enough stock
        if (orderDetailsDTO.getQuantity() > product.getStock()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        // Save the OrderDetails entity to the repository
        OrderDetails savedOrderDetails;
        try {
            savedOrderDetails = orderDetailsRepository.save(orderDetailsToStore);
            // Update product stock
            product.setStock(product.getStock() - orderDetailsDTO.getQuantity());
            productRepository.save(product);
            // Set the ID of the DTO to the ID of the saved OrderDetails entity
            orderDetailsDTO.setId(savedOrderDetails.getId());
            return orderDetailsDTO;
        } catch (Exception e) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to create new order details for orders that already exist
    @Override
    @Transactional
    public OrderDetailsDTO createOrderDetailsToExistingOrder(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Fetch the order by order ID
            Order order = orderRepository.findById(orderDetailsDTOAddition.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderDetailsDTOAddition.getIdOrder()));

            // Check if the order belongs to the logged-in user
            if (!order.getIdCustomer().equals(idCustomer)) {
                throw new UnauthorizedAccessException("You are not authorized to add details to this order");
            }

            // Fetch the product details by product ID
            Product product = productRepository.findById(orderDetailsDTOAddition.getIdProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderDetailsDTOAddition.getIdProduct()));

            // Fetch the price of a single product unit
            Double pricePerUnit = product.getPrice();

            // Calculate the total price for the order detail
            Double totalPrice = pricePerUnit * orderDetailsDTOAddition.getQuantity();

            // Check if there is enough stock
            if (orderDetailsDTOAddition.getQuantity() > product.getStock()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Calculate the updated total price for the order
            Double updatedTotalPrice = order.getTotalPrice() + totalPrice;

            // Update the total price of the order in the database using the custom query method
            orderRepository.updateTotalPriceById(updatedTotalPrice, order.getId());

            // Create a new OrderDetails entity
            OrderDetails orderDetailsToStore = new OrderDetails();
            orderDetailsToStore.setIdOrder(order.getId());
            orderDetailsToStore.setIdProduct(orderDetailsDTOAddition.getIdProduct());
            orderDetailsToStore.setPrice(pricePerUnit);
            orderDetailsToStore.setQuantity(orderDetailsDTOAddition.getQuantity());

            // Save the OrderDetails entity to the repository
            OrderDetails savedOrderDetails = orderDetailsRepository.save(orderDetailsToStore);

            // Update product stock
            product.setStock(product.getStock() - orderDetailsDTOAddition.getQuantity());
            productRepository.save(product);

            // Return the OrderDetailsDTO with the generated ID
            return new OrderDetailsDTO(
                    savedOrderDetails.getId(),
                    savedOrderDetails.getIdOrder(),
                    savedOrderDetails.getIdProduct(),
                    savedOrderDetails.getPrice(),
                    savedOrderDetails.getQuantity()
            );
        } catch (Exception e) {
            // If any other exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update existing order details
    @Override
    @Transactional
    public OrderDetailsDTO adminUpdateOrderDetails(OrderDetailsDTO orderDetailsDTO) {
        try {
            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsDTO.getId()));

            // Fetch the associated order
            Order existingOrder = orderRepository.findById(existingOrderDetails.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + existingOrderDetails.getIdOrder()));

            // Calculate the total price difference
            Double oldTotalPrice = existingOrder.getTotalPrice();
            Double oldOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();
            Double newOrderDetailPrice = orderDetailsDTO.getPrice() * orderDetailsDTO.getQuantity();
            Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;

            // Update the fields of the existing order details
            existingOrderDetails.setIdOrder(orderDetailsDTO.getIdOrder());
            existingOrderDetails.setIdProduct(orderDetailsDTO.getIdProduct());
            existingOrderDetails.setPrice(orderDetailsDTO.getPrice());
            existingOrderDetails.setQuantity(orderDetailsDTO.getQuantity());

            // Fetch the product
            Product product = productRepository.findById(existingOrderDetails.getIdProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + existingOrderDetails.getIdProduct()));

            // Calculate the quantity difference
            int quantityDifference = existingOrderDetails.getQuantity() - orderDetailsDTO.getQuantity();

            // Update product stock
            int newStock = product.getStock() + quantityDifference;
            if (newStock < 0) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
            product.setStock(newStock);
            productRepository.save(product);

            // Save the updated order details to the repository
            OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

            // Update the total price of the order
            Double updatedTotalPrice = oldTotalPrice + priceDifference;
            orderRepository.updateTotalPriceById(updatedTotalPrice, existingOrder.getId());

            // Convert the updated order details back to a DTO and return it
            return convertOrderDetailsToDTO(savedOrderDetails);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating OrderDetails", ex);
        }
    }

    @Override
    @Transactional
    public OrderDetailsDTO updateOrderDetails(OrderDetailsDTOUpdate orderDetailsDTOUpdate) {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsDTOUpdate.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsDTOUpdate.getId()));

            // Fetch the associated order
            Order existingOrder = orderRepository.findById(existingOrderDetails.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + existingOrderDetails.getIdOrder()));

            // Ensure the order belongs to the logged-in user
            if (!existingOrder.getIdCustomer().equals(idCustomer)) {
                throw new UnauthorizedAccessException("Order does not belong to the logged-in user");
            }

            // Check if the order ID matches
            if (!existingOrderDetails.getIdOrder().equals(orderDetailsDTOUpdate.getIdOrder())) {
                throw new BadRequestException("Order ID does not match the order details");
            }

            // Fetch the product ID from the existing order details
            Integer productId = existingOrderDetails.getIdProduct();

            // Fetch the product details by product ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

            // Calculate the quantity difference
            int quantityDifference = existingOrderDetails.getQuantity() - orderDetailsDTOUpdate.getQuantity();

            // Update product stock
            int newStock = product.getStock() + quantityDifference;
            if (newStock < 0) {
                throw new InsufficientStockException("Not enough stock for product: " + product.getName());
            }
            product.setStock(newStock);
            productRepository.save(product);

            // Update the quantity of the existing order details
            existingOrderDetails.setQuantity(orderDetailsDTOUpdate.getQuantity());

            // Save the updated order details to the repository
            OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

            // Update the total price of the order
            Double oldTotalPrice = existingOrder.getTotalPrice();
            Double oldOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();
            Double newOrderDetailPrice = product.getPrice() * orderDetailsDTOUpdate.getQuantity();
            Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;
            Double updatedTotalPrice = oldTotalPrice + priceDifference;
            orderRepository.updateTotalPriceById(updatedTotalPrice, existingOrder.getId());

            // Convert the updated order details back to a DTO and return it
            return convertOrderDetailsToDTO(savedOrderDetails);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating OrderDetails", ex);
        }
    }

    @Override
    @Transactional
    public void adminForceDeleteOrderDetails(Integer orderDetailsId) {
        try {
            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));

            // Fetch the order ID
            Integer orderId = existingOrderDetails.getIdOrder();

            // Fetch the product ID
            Integer productId = existingOrderDetails.getIdProduct();

            // Fetch the order
            Order order = orderRepository.findById(orderId)
                    .orElse(null);

            // Fetch the product
            Product product = productRepository.findById(productId)
                    .orElse(null);

            // Update order total price if the order exists
            if (order != null) {
                order.setTotalPrice(order.getTotalPrice() - (existingOrderDetails.getPrice() * existingOrderDetails.getQuantity()));

                // Check if the associated order has any other order details
                List<OrderDetails> otherOrderDetails = orderDetailsRepository.findAllByIdOrder(orderId);
                if (otherOrderDetails.size() == 1) {
                    // If there are no other order details associated, delete both the order details and the associated order
                    orderDetailsRepository.delete(existingOrderDetails);
                    orderRepository.delete(order);
                    return; // Exit early if the associated order is deleted
                } else {
                    orderRepository.save(order);
                }
            }

            // Update product stock if the product exists
            if (product != null) {
                product.setStock(product.getStock() + existingOrderDetails.getQuantity());
                productRepository.save(product);
            }

            // Delete the order details from the repository
            orderDetailsRepository.delete(existingOrderDetails);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting OrderDetails", ex);
        }
    }

    @Override
    @Transactional
    public void adminDeleteOrderDetails(Integer orderDetailsId) {
        try {
            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));

            // Fetch the associated order
            Order associatedOrder = orderRepository.findById(existingOrderDetails.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + existingOrderDetails.getIdOrder()));

            // Fetch the product ID
            Integer productId = existingOrderDetails.getIdProduct();

            // Fetch the product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

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
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting OrderDetails", ex);
        }
    }

    // Method to delete order details by ID
    @Override
    @Transactional
    public void deleteOrderDetails(Integer orderDetailsId) {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));

            // Fetch the associated order
            Order associatedOrder = orderRepository.findById(existingOrderDetails.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + existingOrderDetails.getIdOrder()));

            // Check if the order belongs to the logged-in user
            if (!associatedOrder.getIdCustomer().equals(idCustomer)) {
                throw new UnauthorizedAccessException("You are not authorized to delete details of this order");
            }

            // Fetch the product ID
            Integer productId = existingOrderDetails.getIdProduct();

            // Fetch the product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

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
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting OrderDetails", ex);
        }
    }

    // Method to convert DTO to OrderDetails entity
    @Override
    public OrderDetails convertDTOToOrderDetails(OrderDetailsDTO orderDetailsDTO) {
        try {
            // Check if any required fields in the DTO are null
            if (Stream.of(orderDetailsDTO.getIdOrder(), orderDetailsDTO.getIdProduct(), orderDetailsDTO.getPrice(), orderDetailsDTO.getQuantity())
                    .anyMatch(Objects::isNull)) {
                throw new BadRequestException("The received request does not have the correct format.");
            }

            // Create a new OrderDetails entity and set its fields
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setId(orderDetailsDTO.getId());
            orderDetails.setIdOrder(orderDetailsDTO.getIdOrder());
            orderDetails.setIdProduct(orderDetailsDTO.getIdProduct());
            orderDetails.setPrice(orderDetailsDTO.getPrice());
            orderDetails.setQuantity(orderDetailsDTO.getQuantity());

            // Check if the associated Order and Product entities exist
            Optional.ofNullable(orderRepository.findById(orderDetailsDTO.getIdOrder())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderDetailsDTO.getIdOrder())));
            Optional.ofNullable(productRepository.findById(orderDetailsDTO.getIdProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderDetailsDTO.getIdProduct())));

            return orderDetails;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting OrderDetailsDTO to OrderDetails", ex);
        }
    }

    // Method to convert OrderDetails entity to DTO
    @Override
    public OrderDetailsDTO convertOrderDetailsToDTO(OrderDetails orderDetails) {
        try {
            // Create a new OrderDetailsDTO and set its fields
            OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO();
            orderDetailsDTO.setId(orderDetails.getId());
            orderDetailsDTO.setIdOrder(orderDetails.getIdOrder());
            orderDetailsDTO.setIdProduct(orderDetails.getIdProduct());
            orderDetailsDTO.setPrice(orderDetails.getPrice());
            orderDetailsDTO.setQuantity(orderDetails.getQuantity());
            return orderDetailsDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting OrderDetails to OrderDetailsDTO", ex);
        }
    }
}
