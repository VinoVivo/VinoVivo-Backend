package com.mscommerce.service.implementation;

import com.mscommerce.exception.BadRequestException;
import com.mscommerce.exception.InsufficientStockException;
import com.mscommerce.exception.ResourceNotFoundException;
import com.mscommerce.exception.UnauthorizedAccessException;
import com.mscommerce.models.DTO.order.OrderDTO;
import com.mscommerce.models.DTO.order.OrderDTORequest;
import com.mscommerce.models.DTO.order.OrderDTOUpdate;
import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTORequest;
import com.mscommerce.models.Order;
import com.mscommerce.models.OrderDetails;
import com.mscommerce.models.Product;
import com.mscommerce.repositories.jpa.OrderDetailsRepository;
import com.mscommerce.repositories.jpa.OrderRepository;
import com.mscommerce.repositories.jpa.ProductRepository;
import com.mscommerce.security.KeycloakService;
import com.mscommerce.service.IOrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final OrderDetailsRepository orderDetailsRepository;

    private final KeycloakService keycloakService;

    private final CartServiceImpl cartServiceImpl;

    @Value("${spring.mail.username}")
    String vinoVivoEmail;

    @Value("${proyecto.mail-on}")
    String emailOn;

    private final JavaMailSender mailSender;

    /**
     * Fetches all orders for administrators.
     * @return List of all orders.
     */
    @Override
    @Cacheable(value = "orders", key = "#root.method.name")
    public List<OrderDTO> adminGetAllOrders() {
        // Fetch all orders from the repository
        List<Order> orders = orderRepository.findAll();

        // Convert the list of Order entities to a list of DTOs
        return orders.stream()
                .map(this::convertOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches all orders for the current user.
     * @return List of user's orders.
     */
    @Override
    @Cacheable(value = "orders", key = "#root.method.name")
    public List<OrderDTO> getAllOrders() {
        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

        // Fetch all orders for the logged-in user
        List<Order> orders = orderRepository.findByIdCustomer(idCustomer);

        // Convert the list of Order entities to a list of DTOs
        return orders.stream()
                .map(this::convertOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a specific order by ID for administrators.
     * @param orderId ID of the order to be fetched.
     * @return The fetched order.
     */
    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderDTO adminGetOrderById(Integer orderId) throws ResourceNotFoundException {
        // Check if the order exists
        Order existingOrder = getOrderById(orderId);

        // Convert the retrieved Order entity to a DTO
        return convertOrderToOrderDTO(existingOrder);
    }

    /**
     * Creates a new order for administrators.
     * @param orderDTO Contains the new order details.
     * @return The created order.
     */
    @Override
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO adminCreateOrder(OrderDTO orderDTO) throws BadRequestException {
        // Validate the input DTO
        validateOrderDTO(orderDTO);

        // Convert the DTO to an Order entity
        Order orderToStore = convertOrderDTOToOrder(orderDTO);
        orderToStore.setOrderDate(LocalDate.now());

        // Save the Order entity to the repository
        Order savedOrder = orderRepository.save(orderToStore);

        // Set the ID of the DTO to the ID of the saved Order entity
        orderDTO.setId(savedOrder.getId());
        return orderDTO;
    }

    /**
     * Creates a new order for the current user.
     * @param orderDTORequest Contains the new order details.
     * @return The created order.
     */
    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO createOrder(OrderDTORequest orderDTORequest) throws BadRequestException, ResourceNotFoundException, InsufficientStockException, MessagingException, UnsupportedEncodingException {
        // Validate the input DTO
        validateOrderDTORequest(orderDTORequest);

        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

        // Calculate the total price
        Double totalPrice = calculateTotalPrice(orderDTORequest.getOrderDetailsDTORequests());

        // Create a new Order entity and set its fields
        Order order = new Order();
        order.setIdCustomer(idCustomer);
        order.setTotalPrice(totalPrice);
        order.setShippingAddress(orderDTORequest.getShippingAddress());
        order.setOrderEmail(orderDTORequest.getOrderEmail());
        order.setOrderDate(LocalDate.now());

        // Save the Order entity
        Order savedOrder = orderRepository.save(order);

        // Loop through the OrderDetailsDTORequests
        for (OrderDetailsDTORequest orderDetailsDTORequest : orderDTORequest.getOrderDetailsDTORequests()) {
            // Fetch the product by ID from the repository
            Product product = productRepository.findById(orderDetailsDTORequest.getIdProduct())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderDetailsDTORequest.getIdProduct()));

            // Check if there is enough stock
            if (orderDetailsDTORequest.getQuantity() > product.getStock()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            // Create a new OrderDetails entity and set its fields
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setIdOrder(savedOrder.getId());
            orderDetails.setProduct(product);
            orderDetails.setPrice(product.getPrice());
            orderDetails.setQuantity(orderDetailsDTORequest.getQuantity());

            // Save the OrderDetails entity
            orderDetailsRepository.save(orderDetails);

            // Update product stock
            product.setStock(product.getStock() - orderDetailsDTORequest.getQuantity());
            productRepository.save(product);
        }

        // Clean the cart
        cartServiceImpl.cleanCart();

        // Send order confirmation email
        if(emailOn.equals("true")) {
            sendOrderConfirmationEmail(savedOrder);
        }

        // Convert the saved Order entity to a DTO and return
        return convertOrderToOrderDTO(savedOrder);
    }

    /**
     * Updates an existing order for administrators.
     * @param orderDTO Contains the updated order details.
     * @return The updated order.
     */
    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO adminUpdateOrder(OrderDTO orderDTO) throws BadRequestException, ResourceNotFoundException {
        // Validate the input DTO
        validateOrderDTO(orderDTO);

        // Check if the order exists
        Order existingOrder = getOrderById(orderDTO.getId());

        // Convert OrderDTO to Order
        Order updatedOrder = convertOrderDTOToOrder(orderDTO);

        // Update fields of the existing order
        existingOrder.setIdCustomer(updatedOrder.getIdCustomer());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
        existingOrder.setShippingAddress(updatedOrder.getShippingAddress());
        existingOrder.setOrderEmail(updatedOrder.getOrderEmail());
        existingOrder.setOrderDate(updatedOrder.getOrderDate());

        // Save the updated order
        Order savedOrder = orderRepository.save(existingOrder);

        // Convert the updated order back to DTO and return
        return convertOrderToOrderDTO(savedOrder);
    }

    /**
     * Updates an existing order for the current user.
     * @param orderDTO Contains the updated order details.
     * @return The updated order.
     */
    @Override
    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO updateOrder(OrderDTOUpdate orderDTO) throws BadRequestException, ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        // Validate the input DTO
        validateOrderDTOUpdate(orderDTO);

        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

        // Check if the order exists
        Order existingOrder = getOrderById(orderDTO.getId());

        // Verify that the order belongs to the logged-in user
        if (!existingOrder.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("Order does not belong to the logged-in user");
        }

        // Update fields of the existing order
        existingOrder.setShippingAddress(orderDTO.getShippingAddress());
        existingOrder.setOrderEmail(orderDTO.getOrderEmail());

        // Save the updated order
        Order savedOrder = orderRepository.save(existingOrder);

        // Send order update email
        if(emailOn.equals("true")) {
            sendOrderUpdateEmail(savedOrder);
        }

        // Convert the updated order back to DTO and return
        OrderDTO updatedOrderDTO = new OrderDTO();
        updatedOrderDTO.setId(savedOrder.getId());
        updatedOrderDTO.setIdCustomer(savedOrder.getIdCustomer());
        updatedOrderDTO.setTotalPrice(savedOrder.getTotalPrice());
        updatedOrderDTO.setShippingAddress(savedOrder.getShippingAddress());
        updatedOrderDTO.setOrderEmail(savedOrder.getOrderEmail());
        updatedOrderDTO.setOrderDate(savedOrder.getOrderDate());

        return updatedOrderDTO;
    }

    /**
     * Deletes a specific order for administrators.
     * @param orderId ID of the order to be deleted.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
    public void adminDeleteOrder(Integer orderId) throws ResourceNotFoundException {
        // Check if the order exists
        Order existingOrder = getOrderById(orderId);

        // Fetch the associated order details
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(orderId);

        // Update product stock according to the quantity of each order detail
        for (OrderDetails orderDetails : orderDetailsList) {
            // Fetch the product directly from the OrderDetails entity
            Product product = orderDetails.getProduct();

            // Update product stock
            product.setStock(product.getStock() + orderDetails.getQuantity());
            productRepository.save(product);
        }

        // Delete the associated order details
        orderDetailsRepository.deleteAll(orderDetailsList);

        // Delete the order from the repository
        orderRepository.delete(existingOrder);
    }

    /**
     * Deletes a specific order for the current user.
     * @param orderId ID of the order to be deleted.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "orderDetails", allEntries = true),
            @CacheEvict(value = "orders", allEntries = true)
    })
    public void deleteOrder(Integer orderId) throws ResourceNotFoundException, MessagingException, UnsupportedEncodingException {
        // Get the user ID from Keycloak Principal
        String idCustomer = keycloakService.getCustomerIdFromAuthentication();

        // Check if the order exists
        Order existingOrder = getOrderById(orderId);

        // Verify that the order belongs to the logged-in user
        if (!existingOrder.getIdCustomer().equals(idCustomer)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this order");
        }

        // Fetch the associated order details
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(orderId);

        // Update product stock according to the quantity of each order detail
        for (OrderDetails orderDetails : orderDetailsList) {
            // Fetch the product directly from the OrderDetails entity
            Product product = orderDetails.getProduct();

            // Update product stock
            product.setStock(product.getStock() + orderDetails.getQuantity());
            productRepository.save(product);
        }

        // Delete the associated order details
        orderDetailsRepository.deleteAll(orderDetailsList);

        // Delete the order from the repository
        orderRepository.delete(existingOrder);

        // Send order deletion email
        if(emailOn.equals("true")) {
            sendOrderDeletionEmail(existingOrder, orderDetailsList);
        }
    }

    /**
     * Converts an OrderDTO to an Order entity.
     * @param orderDTO DTO to be converted.
     * @return Converted Order entity.
     */
    private Order convertOrderDTOToOrder(OrderDTO orderDTO) {
            // Create a new Order entity and set its fields
            Order order = new Order();
            order.setId(orderDTO.getId());
            order.setIdCustomer(orderDTO.getIdCustomer());
            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setShippingAddress(orderDTO.getShippingAddress());
            order.setOrderEmail(orderDTO.getOrderEmail());
            return order;
    }

    /**
     * Converts an Order to an OrderDTO.
     * @param order Order to be converted.
     * @return Converted OrderDTO.
     */
    private OrderDTO convertOrderToOrderDTO(Order order) {
            // Create a new OrderDTO and set its fields
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setIdCustomer(order.getIdCustomer());
            orderDTO.setTotalPrice(order.getTotalPrice());
            orderDTO.setShippingAddress(order.getShippingAddress());
            orderDTO.setOrderEmail(order.getOrderEmail());
            orderDTO.setOrderDate(order.getOrderDate());
            return orderDTO;
    }

    /**
     * Fetches Order by ID.
     * @param orderId ID of the Order.
     * @return Fetched Order.
     */
    public Order getOrderById(Integer orderId) throws ResourceNotFoundException {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    /**
     * Calculates total price of an order.
     * @param orderDetailsDTORequests List of OrderDetailsDTORequest.
     * @return Total price.
     */
    private Double calculateTotalPrice(List<OrderDetailsDTORequest> orderDetailsDTORequests) {
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

    /**
     * Sends an order confirmation email to the customer.
     * @param order The order for which the confirmation email is to be sent.
     */
    private void sendOrderConfirmationEmail(Order order) throws MessagingException, UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Confirmación de pedido de Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + "Tu pedido ha sido realizado con éxito:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total: $[[totalPrice]]<br>"
                + "Dirección de envío: [[shippingAddress]]<br>"
                + "Fecha del pedido: [[orderDate]]<br><br>"
                + "<b>Detalles del pedido:</b><br>"
                + "[[orderDetails]]"
                + "Gracias por tu compra,<br>"
                + "Vino Vivo";

        // Fetch the associated order details
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(order.getId());

        // Build a string that represents each OrderDetails
        StringBuilder orderDetailsStringBuilder = new StringBuilder();
        for (OrderDetails orderDetails : orderDetailsList) {
            Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();
            orderDetailsStringBuilder.append("Producto: ").append(orderDetails.getProduct().getName())
                    .append("<br> Cantidad: ").append(orderDetails.getQuantity())
                    .append("<br> Precio por unidad: $").append(orderDetails.getPrice())
                    .append("<br> Precio total del detalle: $").append(totalDetailPrice)
                    .append("<br><br>");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", order.getTotalPrice().toString());
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
        content = content.replace("[[orderDate]]", orderDate);
        content = content.replace("[[orderDetails]]", orderDetailsStringBuilder.toString());

        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Sends an email to the customer when an existing order is updated.
     * @param order The order that was updated.
     */
    private void sendOrderUpdateEmail(Order order) throws MessagingException, UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Actualización de pedido de Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + "Los datos de tu pedido han sido actualizados:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total: $[[totalPrice]]<br>"
                + "Dirección de envío: [[shippingAddress]]<br>"
                + "Fecha del pedido: [[orderDate]]<br><br>"
                + "<b>Detalles del pedido:</b><br>"
                + "[[orderDetails]]"
                + "Gracias,<br>"
                + "Vino Vivo";

        // Fetch the associated order details
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findByIdOrder(order.getId());

        // Build a string that represents each OrderDetails
        StringBuilder orderDetailsStringBuilder = new StringBuilder();
        for (OrderDetails orderDetails : orderDetailsList) {
            Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();
            orderDetailsStringBuilder.append("Producto: ").append(orderDetails.getProduct().getName())
                    .append("<br> Cantidad: ").append(orderDetails.getQuantity())
                    .append("<br> Precio por unidad: $").append(orderDetails.getPrice())
                    .append("<br> Precio total del detalle: $").append(totalDetailPrice)
                    .append("<br><br>");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", order.getTotalPrice().toString());
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
        content = content.replace("[[orderDate]]", orderDate);
        content = content.replace("[[orderDetails]]", orderDetailsStringBuilder.toString());

        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * Sends an email to the customer when an existing order is deleted.
     * @param order The order that was deleted.
     */
    private void sendOrderDeletionEmail(Order order, List<OrderDetails> orderDetailsList) throws MessagingException,
            UnsupportedEncodingException {
        String username = keycloakService.getUsernameFromKeycloak();

        String toAddress = order.getOrderEmail();
        String fromAddress = vinoVivoEmail;
        String senderName = "Vino Vivo";
        String subject = "Eliminación de pedido de Vino Vivo";
        String content = "Hola [[name]],<br><br>"
                + "Tu pedido ha sido eliminado:<br><br>"
                + "ID del pedido: [[orderId]]<br>"
                + "Precio total: $[[totalPrice]]<br>"
                + "Dirección de envío: [[shippingAddress]]<br>"
                + "Fecha del pedido: [[orderDate]]<br><br>"
                + "<b>Detalles del pedido:</b><br>"
                + "[[orderDetails]]"
                + "Gracias,<br>"
                + "Vino Vivo";

        // Build a string that represents each OrderDetails
        StringBuilder orderDetailsStringBuilder = new StringBuilder();
        for (OrderDetails orderDetails : orderDetailsList) {
            Double totalDetailPrice = orderDetails.getQuantity() * orderDetails.getPrice();
            orderDetailsStringBuilder.append("Producto: ").append(orderDetails.getProduct().getName())
                    .append("<br> Cantidad: ").append(orderDetails.getQuantity())
                    .append("<br> Precio por unidad: $").append(orderDetails.getPrice())
                    .append("<br> Precio total del detalle: $").append(totalDetailPrice)
                    .append("<br><br>");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", username);
        content = content.replace("[[orderId]]", order.getId().toString());
        content = content.replace("[[totalPrice]]", order.getTotalPrice().toString());
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        String orderDate = order.getOrderDate() != null ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";
        content = content.replace("[[orderDate]]", orderDate);
        content = content.replace("[[orderDetails]]", orderDetailsStringBuilder.toString());

        helper.setText(content, true);

        mailSender.send(message);
    }

    // Validation methods
    /**
     * Validates an OrderDTO.
     * @param orderDTO DTO to be validated.
     */
    private void validateOrderDTO(OrderDTO orderDTO) throws BadRequestException {
        if (orderDTO.getIdCustomer() == null || orderDTO.getIdCustomer().trim().isEmpty()) {
            throw new BadRequestException("Customer ID must not be null or empty");
        }
        if (orderDTO.getTotalPrice() == null || orderDTO.getTotalPrice() < 0) {
            throw new BadRequestException("Total price must not be null and must be greater than or equal to 0");
        }
        if (orderDTO.getShippingAddress() == null || orderDTO.getShippingAddress().trim().isEmpty()) {
            throw new BadRequestException("Shipping address must not be null or empty");
        }
        if (orderDTO.getOrderEmail() == null || orderDTO.getOrderEmail().trim().isEmpty()) {
            throw new BadRequestException("Order email must not be null or empty");
        }
    }

    /**
     * Validates an OrderDTORequest.
     * @param orderDTORequest DTO to be validated.
     */
    private void validateOrderDTORequest(OrderDTORequest orderDTORequest) throws BadRequestException {
        if (orderDTORequest.getShippingAddress() == null || orderDTORequest.getShippingAddress().trim().isEmpty()) {
            throw new BadRequestException("Shipping address must not be null or empty");
        }
        if (orderDTORequest.getOrderEmail() == null || orderDTORequest.getOrderEmail().trim().isEmpty()) {
            throw new BadRequestException("Order email must not be null or empty");
        }
        if (orderDTORequest.getOrderDetailsDTORequests() == null || orderDTORequest.getOrderDetailsDTORequests().isEmpty()) {
            throw new BadRequestException("Order details must not be null or empty");
        }
    }

    /**
     * Validates an OrderDTOUpdate.
     * @param orderDTOUpdate DTO to be validated.
     */
    private void validateOrderDTOUpdate(OrderDTOUpdate orderDTOUpdate) throws BadRequestException {
        if (orderDTOUpdate.getId() == null) {
            throw new BadRequestException("Order ID must not be null");
        }
        if (orderDTOUpdate.getShippingAddress() == null || orderDTOUpdate.getShippingAddress().trim().isEmpty()) {
            throw new BadRequestException("Shipping address must not be null or empty");
        }
        if (orderDTOUpdate.getOrderEmail() == null || orderDTOUpdate.getOrderEmail().trim().isEmpty()) {
            throw new BadRequestException("Order email must not be null or empty");
        }
    }
}







