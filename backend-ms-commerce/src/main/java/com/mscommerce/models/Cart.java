package com.mscommerce.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "id_customer")
    private String idCustomer;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", referencedColumnName = "id")
    @JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "products"})
    private Product product;

    @Column(name = "price")
    private Double price;

    @Column(name = "quantity")
    private Integer quantity;

}
