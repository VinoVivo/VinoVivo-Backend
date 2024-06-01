package com.mscommerce.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "id_customer")
    private String idCustomer;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "order_email")
    private String orderEmail;
}
