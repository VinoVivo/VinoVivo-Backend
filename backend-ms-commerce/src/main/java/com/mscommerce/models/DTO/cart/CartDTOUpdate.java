package com.mscommerce.models.DTO.cart;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTOUpdate {

    private Integer id;

    private Integer quantity;
}
