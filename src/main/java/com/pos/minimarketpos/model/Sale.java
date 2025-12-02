package com.pos.minimarketpos.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id")
    private Long clientId;

    private String clientname;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    private String tax;

    @Column(name = "taxamount", precision = 10, scale = 2)
    private BigDecimal taxamount;

    private String discount;

    @Column(name = "discountamount", precision = 10, scale = 2)
    private BigDecimal discountamount;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(precision = 10, scale = 2)
    private BigDecimal paid;

    @Column(name = "firstpayement", precision = 10, scale = 2)
    private BigDecimal firstpayement;

    private Integer totalitems;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    private Integer status;

    private String paidmethod;

    @Column(name = "register_id")
    private Long registerId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}