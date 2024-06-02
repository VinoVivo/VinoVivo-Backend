package com.mscommerce.models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Integer id;

    private String name;

    private String description;

    private String image;

    private Integer year;

    private Double price;

    private Integer stock;

    private Integer idWinery;

    private Integer idVariety;

    private Integer idType;
}
