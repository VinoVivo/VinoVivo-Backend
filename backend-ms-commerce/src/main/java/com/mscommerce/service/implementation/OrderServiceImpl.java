package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.OrderDTO;
import com.mscommerce.models.DTO.OrderDTORequest;
import com.mscommerce.models.DTO.OrderDTOUpdate;
import com.mscommerce.models.DTO.OrderDetailsDTORequest;
import com.mscommerce.models.Order;
import com.mscommerce.models.OrderDetails;
import com.mscommerce.models.Product;
import com.mscommerce.repositories.OrderDetailsRepository;
import com.mscommerce.repositories.OrderRepository;
import com.mscommerce.repositories.ProductRepository;
import com.mscommerce.service.IOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderDetailsRepository orderDetailsRepository;

    // Method to fetch all orders
    @Override
    public List<OrderDTO> adminGetAllOrders() throws ResourceNotFoundException {
        try {
            // Fetch all orders from the repository
            List<Order> orders = orderRepository.findAll();

            // Convert the list of Order entities to a list of DTOs
            return orders.stream()
                    .map(this::convertOrderToOrderDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch orders");
        }
    }

    @Override
    public List<OrderDTO> getAllOrders() throws ResourceNotFoundException {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Fetch all orders for the logged-in user
            List<Order> orders = orderRepository.findByIdCustomer(idCustomer);

            // Convert the list of Order entities to a list of DTOs
            return orders.stream()
                    .map(this::convertOrderToOrderDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            // If an exception occurs, throw a ResourceNotFoundException
            throw new ResourceNotFoundException("Failed to fetch orders");
        }
    }

    // Method to fetch an order by ID
    @Override
    public OrderDTO adminGetOrderById(Integer orderId) {
        try {
            // Fetch the order by ID from the repository
            Optional<Order> orderOptional = orderRepository.findById(orderId);

            // Check if the order exists
            if (orderOptional.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }

            // Convert the retrieved Order entity to a DTO
            Order order = orderOptional.get();
            return convertOrderToOrderDTO(order);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Order by ID", ex);
        }
    }

    // Method to create a new order
    @Override
    public OrderDTO adminCreateOrder(OrderDTO orderDTO) throws BadRequestException {
        // Convert the DTO to an Order entity
        Order orderToStore = convertOrderDTOToOrder(orderDTO);
        try {
            // Save the Order entity to the repository
            Order savedOrder = orderRepository.save(orderToStore);

            // Set the ID of the DTO to the ID of the saved Order entity
            orderDTO.setId(savedOrder.getId());
            return orderDTO;
        } catch (Exception e) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    @Override
    @Transactional
    public Order createOrder(OrderDTORequest orderDTORequest) throws BadRequestException {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Create an Order entity from OrderDTOFront
            Order order = new Order();
            order.setIdCustomer(idCustomer);
            order.setShippingAddress(orderDTORequest.getShippingAddress());
            order.setOrderEmail(orderDTORequest.getOrderEmail());

            // Calculate total price based on order details
            Double totalPrice = calculateTotalPrice(orderDTORequest.getOrderDetailsDTORequests());
            order.setTotalPrice(totalPrice);

            // Save the Order entity
            Order savedOrder = orderRepository.save(order);

            // Create and save order details
            List<OrderDetails> orderDetailsList = new ArrayList<>();
            for (OrderDetailsDTORequest orderDetailDTO : orderDTORequest.getOrderDetailsDTORequests()) {
                OrderDetails orderDetails = new OrderDetails();
                orderDetails.setIdOrder(savedOrder.getId());
                orderDetails.setIdProduct(orderDetailDTO.getIdProduct());

                // Fetch the product from the database
                Product product = productRepository.findById(orderDetailDTO.getIdProduct())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderDetailDTO.getIdProduct()));

                // Check if the requested quantity exceeds available stock
                if (orderDetailDTO.getQuantity() > product.getStock()) {
                    throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
                }

                // Set order details properties
                orderDetails.setPrice(product.getPrice());
                orderDetails.setQuantity(orderDetailDTO.getQuantity());

                orderDetailsList.add(orderDetails);

                // Update product stock
                product.setStock(product.getStock() - orderDetailDTO.getQuantity());
                productRepository.save(product);
            }
            orderDetailsRepository.saveAll(orderDetailsList);

            return savedOrder;
        } catch (Exception e) {
            // If any other exception occurs, wrap it in a BadRequestException and rethrow
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update an existing order
    @Override
    @Transactional
    public OrderDTO adminUpdateOrder(OrderDTO orderDTO) {
        try {
            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderDTO.getId()));

            // Convert OrderDTO to Order
            Order updatedOrder = convertOrderDTOToOrder(orderDTO);

            // Update fields of the existing order
            existingOrder.setIdCustomer(updatedOrder.getIdCustomer());
            existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
            existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
            existingOrder.setOrderEmail(updatedOrder.getOrderEmail());

            // Save the updated order
            Order savedOrder = orderRepository.save(existingOrder);

            // Convert the updated order back to DTO and return
            return convertOrderToOrderDTO(savedOrder);
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Order", ex);
        }
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(OrderDTOUpdate orderDTO) {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderDTO.getId()));

            // Verify that the order belongs to the logged-in user
            if (!existingOrder.getIdCustomer().equals(idCustomer)) {
                throw new UnauthorizedAccessException("Order does not belong to the logged-in user");
            }

            // Update fields of the existing order
            existingOrder.setShippingAddress(orderDTO.getShippingAddress());
            existingOrder.setOrderEmail(orderDTO.getOrderEmail());

            // Save the updated order
            Order savedOrder = orderRepository.save(existingOrder);

            // Convert the updated order back to DTO and return
            OrderDTO updatedOrderDTO = new OrderDTO();
            updatedOrderDTO.setId(savedOrder.getId());
            updatedOrderDTO.setIdCustomer(savedOrder.getIdCustomer());
            updatedOrderDTO.setTotalPrice(savedOrder.getTotalPrice());
            updatedOrderDTO.setShippingAddress(savedOrder.getShippingAddress());
            updatedOrderDTO.setOrderEmail(savedOrder.getOrderEmail());

            return updatedOrderDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Order", ex);
        }
    }

    public void adminDeleteOrder(Integer orderId) {
        try {
            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

            // Fetch the associated order details
            List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(orderId);

            // Update product stock according to the quantity of each order detail
            for (OrderDetails orderDetails : orderDetailsList) {
                Integer productId = orderDetails.getIdProduct();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

                int newStock = product.getStock() + orderDetails.getQuantity();
                product.setStock(newStock);
                productRepository.save(product);
            }

            // Delete the associated order details
            orderDetailsRepository.deleteAll(orderDetailsList);

            // Delete the order from the repository
            orderRepository.delete(existingOrder);
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Order", ex);
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Integer orderId) {
        try {
            // Get the user ID from Keycloak Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String idCustomer = authentication.getName();

            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

            // Verify that the order belongs to the logged-in user
            if (!existingOrder.getIdCustomer().equals(idCustomer)) {
                throw new UnauthorizedAccessException("You are not authorized to delete this order");
            }

            // Fetch the associated order details
            List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(orderId);

            // Update product stock according to the quantity of each order detail
            for (OrderDetails orderDetails : orderDetailsList) {
                Integer productId = orderDetails.getIdProduct();
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

                int newStock = product.getStock() + orderDetails.getQuantity();
                product.setStock(newStock);
                productRepository.save(product);
            }

            // Delete the associated order details
            orderDetailsRepository.deleteAll(orderDetailsList);

            // Delete the order from the repository
            orderRepository.delete(existingOrder);
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Order", ex);
        }
    }

    // Method to convert DTO to Order entity
    @Override
    public Order convertOrderDTOToOrder(OrderDTO orderDTO) {
        try {
            // Check if any required fields in the DTO are null
            if (Objects.isNull(orderDTO.getIdCustomer()) || Objects.isNull(orderDTO.getTotalPrice()) ||
                    Objects.isNull(orderDTO.getShippingAddress()) || Objects.isNull(orderDTO.getOrderEmail())) {
                throw new BadRequestException("The received request does not have the correct format.");
            }

            // Create a new Order entity and set its fields
            Order order = new Order();
            order.setId(orderDTO.getId());
            order.setIdCustomer(orderDTO.getIdCustomer());
            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setShippingAddress(orderDTO.getShippingAddress());
            order.setOrderEmail(orderDTO.getOrderEmail());

            return order;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting OrderDTO to Order", ex);
        }
    }

    // Method to convert Order entity to DTO
    @Override
    public OrderDTO convertOrderToOrderDTO(Order order) {
        try {
            // Create a new OrderDTO and set its fields
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setIdCustomer(order.getIdCustomer());
            orderDTO.setTotalPrice(order.getTotalPrice());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setOrderEmail(order.getOrderEmail());
            return orderDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Order to OrderDTO", ex);
        }
    }

    @Override
    public Double calculateTotalPrice(List<OrderDetailsDTORequest> orderDetailsDTORequests) {
        double totalPrice = 0.0;
        for (OrderDetailsDTORequest orderDetailDTO : orderDetailsDTORequests) {
            // Retrieve the product details from the database based on the idProduct
            Product product = productRepository.findById(orderDetailDTO.getIdProduct())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + orderDetailDTO.getIdProduct()));

            // Calculate the total price by multiplying the quantity with the price of the product
            totalPrice += orderDetailDTO.getQuantity() * product.getPrice();
        }
        return totalPrice;
    }
}


