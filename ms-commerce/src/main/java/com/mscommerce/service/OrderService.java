package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDTO;
import com.mscommerce.models.Order;
import com.mscommerce.repositories.OrderRepository;
import com.mscommerce.repositories.implementation.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderRepository {

    private final OrderRepository orderRepository;

    // Method to fetch all orders
    @Override
    public List<OrderDTO> getAllOrders() throws ResourceNotFoundException {
        try {
            // Retrieve all orders from the repository
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

    // Method to fetch an order by ID
    @Override
    public OrderDTO getOrderById(Integer orderId) throws ResourceNotFoundException {
        try {
            // Retrieve the order by ID from the repository
            Optional<Order> orderOptional = orderRepository.findById(orderId);

            // Check if the order exists
            if (orderOptional.isEmpty()) {
                throw new ResourceNotFoundException("Order not found with ID: " + orderId);
            }

            // Convert the retrieved Order entity to a DTO
            Order order = orderOptional.get();
            return convertOrderToOrderDTO(order);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting Order by ID", ex);
        }
    }

    // Method to create a new order
    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) throws BadRequestException {
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

    // Method to update an existing order
    @Override
    public OrderDTO updateOrder(OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException {
        try {
            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderDTO.getId()));

            // Convert OrderDTO to Order
            Order updatedOrder = convertOrderDTOToOrder(orderDTO);

            // Update fields of the existing order
            existingOrder.setIdCustomer(updatedOrder.getIdCustomer());
            existingOrder.setAmount(updatedOrder.getAmount());
            existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
            existingOrder.setOrderEmail(updatedOrder.getOrderEmail());

            // Save the updated order
            Order savedOrder = orderRepository.save(existingOrder);

            // Convert the updated order back to DTO and return
            return convertOrderToOrderDTO(savedOrder);
        } catch (BadRequestException | ResourceNotFoundException ex) {
            // If a BadRequestException or ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating Order", ex);
        }
    }

    // Method to delete an order by ID
    @Override
    public void deleteOrder(Integer orderId) throws ResourceNotFoundException {
        try {
            // Check if the order exists
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

            // Delete the order from the repository
            orderRepository.delete(existingOrder);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting Order", ex);
        }
    }

    // Method to convert DTO to Order entity
    @Override
    public Order convertOrderDTOToOrder(OrderDTO orderDTO) throws BadRequestException {
        try {
            // Check if any required fields in the DTO are null
            if (Objects.isNull(orderDTO.getIdCustomer()) || Objects.isNull(orderDTO.getAmount()) ||
                    Objects.isNull(orderDTO.getShippingAddress()) || Objects.isNull(orderDTO.getOrderEmail())) {
                throw new BadRequestException("The received request does not have the correct format.");
            }

            // Create a new Order entity and set its fields
            Order order = new Order();
            order.setId(orderDTO.getId());
            order.setIdCustomer(orderDTO.getIdCustomer());
            order.setAmount(orderDTO.getAmount());
            order.setShippingAddress(orderDTO.getShippingAddress());
            order.setOrderEmail(orderDTO.getOrderEmail());

            return order;
        } catch (BadRequestException ex) {
            // If a BadRequestException occurs, rethrow it
            throw ex;
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
            orderDTO.setAmount(order.getAmount());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setOrderEmail(order.getOrderEmail());
            return orderDTO;
        } catch (Exception ex) {
            // If any exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while converting Order to OrderDTO", ex);
        }
    }
}


