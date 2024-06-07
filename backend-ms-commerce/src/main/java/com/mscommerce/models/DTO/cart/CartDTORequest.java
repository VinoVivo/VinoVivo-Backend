package com.mscommerce.models.DTO.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTORequest {

    private Integer idProduct;

    private Integer quantity;

}
