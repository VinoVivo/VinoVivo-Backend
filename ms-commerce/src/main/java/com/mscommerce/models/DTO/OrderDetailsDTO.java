package com.mscommerce.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDTO {

    private Integer id;

    private Integer idOrder;

    private Integer idProduct;

    private Double price;

    private Integer quantity;
}
