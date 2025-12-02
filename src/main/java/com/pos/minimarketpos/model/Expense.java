package com.pos.minimarketpos.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String reference;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private Boolean lunas;
    private String note;
    private String attachment;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}