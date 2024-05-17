package com.mscommerce.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
    private Integer idCustomer;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "order_email")
    private String orderEmail;
}
