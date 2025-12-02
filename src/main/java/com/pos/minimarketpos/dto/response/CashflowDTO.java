package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CashflowDTO {
    private Long id;
    private LocalDate date;
    private String reference;
    private Long categoryId;
    private String categoryName;
    private Long storeId;
    private String storeName;
    private BigDecimal amount;
    private Boolean lunas;
    private String note;
    private String attachment;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdDate;
}

