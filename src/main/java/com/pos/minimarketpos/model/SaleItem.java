package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private Integer qt;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "sale_id")
    private Long saleId;

    private LocalDateTime date;
}