package com.pos.minimarketpos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Register {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "cash_inhand", precision = 10, scale = 2)
    private BigDecimal cashInhand;

    @Column(name = "cash_total", precision = 10, scale = 2)
    private BigDecimal cashTotal;

    @Column(name = "cash_sub", precision = 10, scale = 2)
    private BigDecimal cashSub;

    @Column(name = "cc_total", precision = 10, scale = 2)
    private BigDecimal ccTotal;

    @Column(name = "cc_sub", precision = 10, scale = 2)
    private BigDecimal ccSub;

    @Column(name = "cheque_total", precision = 10, scale = 2)
    private BigDecimal chequeTotal;

    @Column(name = "cheque_sub", precision = 10, scale = 2)
    private BigDecimal chequeSub;

    private String note;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    private LocalDateTime date;
    private Integer status;
}