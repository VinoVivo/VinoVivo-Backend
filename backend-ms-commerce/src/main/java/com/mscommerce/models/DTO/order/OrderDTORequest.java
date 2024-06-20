package com.mscommerce.models.DTO.order;

import com.mscommerce.models.DTO.orderDetails.OrderDetailsDTORequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTORequest {

    private String shippingAddress;

    private String orderEmail;

    private List<OrderDetailsDTORequest> orderDetailsDTORequests;
}
