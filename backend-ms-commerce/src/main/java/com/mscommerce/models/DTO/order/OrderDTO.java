package com.mscommerce.models.DTO.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer id;

    private String idCustomer;

    private Double totalPrice;

    private String shippingAddress;

    private String orderEmail;

    private String orderDate;

    public void setOrderDate(LocalDate orderDate) {
        if(orderDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            this.orderDate = orderDate.format(formatter);
        }
    }
}
