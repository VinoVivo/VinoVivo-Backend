package com.mscommerce.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTOUpdate {

    private Integer id;

    private String shippingAddress;

    private String orderEmail;
}
