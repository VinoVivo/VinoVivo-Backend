package com.mscommerce.models.DTO.orderDetails;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDTOUpdate {

    private Integer id;

    private Integer idOrder;

    private Integer quantity;

}
