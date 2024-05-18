package com.mscommerce.service;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.models.DTO.OrderDetailsDTO;
import com.mscommerce.models.OrderDetails;
import com.mscommerce.repositories.OrderDetailsRepository;
import com.mscommerce.repositories.OrderRepository;
import com.mscommerce.repositories.ProductRepository;
import com.mscommerce.repositories.implementation.IOrderDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderDetailsService implements IOrderDetailsRepository {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // Method to fetch all order details
    @Override
    public List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException {
        try {
            // Retrieve all order details from the repository
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

    // Method to fetch order details by ID
    @Override
    public OrderDetailsDTO getOrderDetailsById(Integer orderDetailsId) throws ResourceNotFoundException {
        try {
            // Retrieve the order details by ID from the repository
            Optional<OrderDetails> orderDetailsOptional = orderDetailsRepository.findById(orderDetailsId);
            // Check if the order details exist
            if (orderDetailsOptional.isEmpty()) {
                throw new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId);
            }
            // Convert the retrieved OrderDetails entity to a DTO
            OrderDetails orderDetails = orderDetailsOptional.get();
            return convertOrderDetailsToDTO(orderDetails);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while getting OrderDetails by ID", ex);
        }
    }

    // Method to create new order details
    @Override
    public OrderDetailsDTO createOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        // Convert the DTO to an OrderDetails entity
        OrderDetails orderDetailsToStore = convertDTOToOrderDetails(orderDetailsDTO);

        // Save the OrderDetails entity to the repository
        OrderDetails savedOrderDetails;
        try {
            savedOrderDetails = orderDetailsRepository.save(orderDetailsToStore);
            // Set the ID of the DTO to the ID of the saved OrderDetails entity
            orderDetailsDTO.setId(savedOrderDetails.getId());
            return orderDetailsDTO;
        } catch (Exception e) {
            // If an exception occurs, throw a BadRequestException
            throw new BadRequestException("The received request does not have the correct format.");
        }
    }

    // Method to update existing order details
    @Override
    public OrderDetailsDTO updateOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException, ResourceNotFoundException {
        try {
            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsDTO.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsDTO.getId()));

            // Convert the DTO to an OrderDetails entity
            OrderDetails updatedOrderDetails = convertDTOToOrderDetails(orderDetailsDTO);

            // Update the fields of the existing order details
            existingOrderDetails.setIdOrder(updatedOrderDetails.getIdOrder());
            existingOrderDetails.setIdProduct(updatedOrderDetails.getIdProduct());
            existingOrderDetails.setPrice(updatedOrderDetails.getPrice());
            existingOrderDetails.setQuantity(updatedOrderDetails.getQuantity());

            // Save the updated order details to the repository
            OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

            // Convert the updated order details back to a DTO and return it
            return convertOrderDetailsToDTO(savedOrderDetails);
        } catch (BadRequestException | ResourceNotFoundException ex) {
            // If a BadRequestException or ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while updating OrderDetails", ex);
        }
    }

    // Method to delete order details by ID
    @Override
    public void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException {
        try {
            // Check if the order details exist
            OrderDetails existingOrderDetails = orderDetailsRepository.findById(orderDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException("OrderDetails not found with ID: " + orderDetailsId));

            // Delete the order details from the repository
            orderDetailsRepository.delete(existingOrderDetails);
        } catch (ResourceNotFoundException ex) {
            // If a ResourceNotFoundException occurs, rethrow it
            throw ex;
        } catch (Exception ex) {
            // If any other exception occurs, wrap it in a RuntimeException and rethrow
            throw new RuntimeException("Error occurred while deleting OrderDetails", ex);
        }
    }

    // Method to convert DTO to OrderDetails entity
    @Override
    public OrderDetails convertDTOToOrderDetails(OrderDetailsDTO orderDetailsDTO) throws BadRequestException,
            ResourceNotFoundException {
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
        } catch (BadRequestException | ResourceNotFoundException ex) {
            // If a BadRequestException or ResourceNotFoundException occurs, rethrow it
            throw ex;
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
