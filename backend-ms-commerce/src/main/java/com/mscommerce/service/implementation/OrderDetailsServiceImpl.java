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
import com.mscommerce.security.KeycloakService;
import com.mscommerce.service.IOrderDetailsService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDetailsServiceImpl implements IOrderDetailsService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final OrderServiceImpl orderService;

    private final KeycloakService keycloakService;

    @Value("${spring.mail.username}")
    String vinoVivoEmail;

    @Value("${proyecto.mail-on}")
    String emailOn;

    private final JavaMailSender mailSender;

    /**
     * Fetches all order details for administrators.
     * @return List of all order details.
     */
    @Override
    @Cacheable(value = "orderDetails", key = "#root.method.name")
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
    @Cacheable(value = "orderDetails", key = "#root.method.name")
    public List<OrderDetailsDTO> getAllOrderDetails() throws ResourceNotFoundException {
        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

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
    @Cacheable(value = "orderDetails", key = "#orderId")
    public List<OrderDetailsDTO> getOrderDetailsByOrderId(Integer orderId) throws ResourceNotFoundException {
        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

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
    @Cacheable(value = "orderDetails", key = "#orderDetailsId")
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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
    public OrderDetailsDTO createOrderDetailsToExistingOrder(OrderDetailsDTOAddition orderDetailsDTOAddition) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        // Validate the input DTO
        validateOrderDetailsDTOAddition(orderDetailsDTOAddition);

        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

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

        // Send order details creation email
        if(emailOn.equals("true")) {
            sendOrderDetailsCreationEmail(order, savedOrderDetails, updatedTotalPrice);
        }

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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
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
        OrderDetails updatedOrderDetails = convertDTOToOrderDetails(orderDetailsDTO);

        // Calculate the new price of the order detail
        Double newOrderDetailPrice = updatedOrderDetails.getPrice() * updatedOrderDetails.getQuantity();

        // Calculate the price difference
        Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;

        // Fetch the product
        Product product = updatedOrderDetails.getProduct();

        // Calculate the quantity difference
        int quantityDifference = updatedOrderDetails.getQuantity() - orderDetailsDTO.getQuantity();

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
        OrderDetails savedOrderDetails = orderDetailsRepository.save(updatedOrderDetails);

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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
    public OrderDetailsDTO updateOrderDetails(OrderDetailsDTOUpdate orderDetailsDTOUpdate) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        // Validate the input DTO
        validateOrderDetailsDTOUpdate(orderDetailsDTOUpdate);

        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

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

        // Calculate the old total price of the order detail
        Double oldOrderDetailPrice = existingOrderDetails.getPrice() * existingOrderDetails.getQuantity();

        // Update the quantity of the order details
        existingOrderDetails.setQuantity(orderDetailsDTOUpdate.getQuantity());

        // Calculate the new total price of the order detail
        Double newOrderDetailPrice = product.getPrice() * orderDetailsDTOUpdate.getQuantity();

        // Save the updated order details to the repository
        OrderDetails savedOrderDetails = orderDetailsRepository.save(existingOrderDetails);

        // Calculate the price difference
        Double priceDifference = newOrderDetailPrice - oldOrderDetailPrice;

        // Calculate the updated total price of the order
        Double updatedTotalPrice = existingOrder.getTotalPrice() + priceDifference;

        // Update the total price of the order in the repository
        orderRepository.updateTotalPriceById(updatedTotalPrice, orderDetailsDTOUpdate.getIdOrder());

        // Send order details update email
        if(emailOn.equals("true")) {
            sendOrderDetailsUpdateEmail(existingOrder, savedOrderDetails, updatedTotalPrice);
        }

        // Convert the updated order details back to a DTO and return it
        return convertOrderDetailsToDTO(savedOrderDetails);
    }

    /**
     * Deletes a specific order detail for administrators even if the order associated with it doesn't exist.
     * @param orderDetailsId ID of the order detail to be deleted.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
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
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
    public void deleteOrderDetails(Integer orderDetailsId) throws ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

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

        Double updatedTotalPrice = 0.0;

        // If there are no other order details associated with the order
        if (orderDetailsList.size() == 1) {
            // Delete the order details and the associated order
            orderDetailsRepository.delete(existingOrderDetails);
            orderRepository.delete(associatedOrder);
        } else {
            // Calculate the updated total price of the order
            updatedTotalPrice = associatedOrder.getTotalPrice() - (existingOrderDetails.getPrice() * existingOrderDetails.getQuantity());

            // Update the total price of the order in the repository
            orderRepository.updateTotalPriceById(updatedTotalPrice, associatedOrder.getId());

            // Delete the order details
            orderDetailsRepository.delete(existingOrderDetails);
        }

        // Send order details deletion email
        if(emailOn.equals("true")) {
            sendOrderDetailsDeletionEmail(associatedOrder, existingOrderDetails, updatedTotalPrice);
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

    /**
     * Sends an email to the customer when a new OrderDetails is created.
     * @param order The order associated with the OrderDetails.
     * @param orderDetails The OrderDetails that was created.
     */
    private void sendOrderDetailsCreationEmail(Order order, OrderDetails orderDetails, Double totalPrice) throws MessagingException, UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Creación de detalles de pedido en Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + "Se ha añadido un nuevo detalle a tu pedido:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total del pedido: [[totalPrice]]<br><br>"
                + "<b>Nuevo detalle del pedido:</b><br>"
                + "ID del detalle del pedido: [[orderDetailsId]]<br>"
                + "Producto: [[productName]]<br>"
                + "Cantidad: [[quantity]]<br>"
                + "Precio por unidad: [[price]]<br>"
                + "Precio total del detalle: [[totalDetailPrice]]<br><br>"
                + "Gracias,<br>"
                + "Vino Vivo";

        Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", totalPrice.toString());
        content = content.replace("[[orderDetailsId]]", orderDetails.getId().toString());
        content = content.replace("[[productName]]", orderDetails.getProduct().getName());
        content = content.replace("[[quantity]]", String.valueOf(orderDetails.getQuantity()));
        content = content.replace("[[price]]", orderDetails.getPrice().toString());
        content = content.replace("[[totalDetailPrice]]", totalDetailPrice.toString());

        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Sends an email to the customer when an existing OrderDetails is updated.
     * @param order The order associated with the OrderDetails.
     * @param orderDetails The OrderDetails that was updated.
     */
    private void sendOrderDetailsUpdateEmail(Order order, OrderDetails orderDetails, Double totalPrice) throws MessagingException, UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Actualización de detalles de pedido en Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + "Se ha actualizado un detalle de tu pedido:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total del pedido: [[totalPrice]]<br><br>"
                + "<b>Detalle actualizado del pedido:</b><br>"
                + "ID del detalle del pedido: [[orderDetailsId]]<br>"
                + "Producto: [[productName]]<br>"
                + "Cantidad: [[quantity]]<br>"
                + "Precio por unidad: [[price]]<br>"
                + "Precio total del detalle: [[totalDetailPrice]]<br><br>"
                + "Gracias,<br>"
                + "Vino Vivo";

        Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", totalPrice.toString());
        content = content.replace("[[orderDetailsId]]", orderDetails.getId().toString());
        content = content.replace("[[productName]]", orderDetails.getProduct().getName());
        content = content.replace("[[quantity]]", String.valueOf(orderDetails.getQuantity()));
        content = content.replace("[[price]]", orderDetails.getPrice().toString());
        content = content.replace("[[totalDetailPrice]]", totalDetailPrice.toString());

        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Sends an email to the customer when an existing OrderDetails is deleted.
     * @param order The order associated with the OrderDetails.
     * @param orderDetails The OrderDetails that was deleted.
     */
    private void sendOrderDetailsDeletionEmail(Order order, OrderDetails orderDetails, Double totalPrice) throws MessagingException,
            UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Eliminación de detalles de pedido en Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + ":Se ha eliminado un detalle de tu pedido:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total del pedido: [[totalPrice]]<br><br>"
                + "<b>Detalle eliminado del pedido:</b><br>"
                + "ID del detalle del pedido: [[orderDetailsId]]<br>"
                + "Producto: [[productName]]<br>"
                + "Cantidad: [[quantity]]<br>"
                + "Precio por unidad: [[price]]<br>"
                + "Precio total del detalle: [[totalDetailPrice]]<br><br>"
                + "Gracias,<br>"
                + "Vino Vivo";

        Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", totalPrice.toString());
        content = content.replace("[[orderDetailsId]]", orderDetails.getId().toString());
        content = content.replace("[[productName]]", orderDetails.getProduct().getName());
        content = content.replace("[[quantity]]", String.valueOf(orderDetails.getQuantity()));
        content = content.replace("[[price]]", orderDetails.getPrice().toString());
        content = content.replace("[[totalDetailPrice]]", totalDetailPrice.toString());

        helper.setText(content, true);

        mailSender.send(message);
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
