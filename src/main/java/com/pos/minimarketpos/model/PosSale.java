package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "posales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer number;

    @Column(name = "register_id")
    private Long registerId;

    private Integer qt;
    private Integer status;
}