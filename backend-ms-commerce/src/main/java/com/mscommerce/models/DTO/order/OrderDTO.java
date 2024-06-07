package com.mscommerce.models.DTO.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
