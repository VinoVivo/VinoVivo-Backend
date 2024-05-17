package com.mscommerce.models.DTO;

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

    private Integer idCustomer;

    private Double amount;

    private String shippingAddress;

    private String orderEmail;
}
