package com.mscommerce.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private Integer id;

    private String idCustomer;

    private String idProduct;

    private Integer quantity;

    private Double price;
}
