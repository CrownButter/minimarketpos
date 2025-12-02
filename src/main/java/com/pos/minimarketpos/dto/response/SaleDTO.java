package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleDTO {
    private Long id;
    private Long clientId;
    private String clientname;
    private BigDecimal cost;
    private BigDecimal subtotal;
    private String tax;
    private BigDecimal taxamount;
    private String discount;
    private BigDecimal discountamount;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal firstpayement;
    private Integer totalitems;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer status;
    private String paidmethod;
    private Long registerId;
}
