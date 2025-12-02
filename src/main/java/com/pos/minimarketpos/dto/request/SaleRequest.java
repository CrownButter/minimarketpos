package com.pos.minimarketpos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleRequest {
    private Long clientId;
    private String clientname;

    @NotNull
    private BigDecimal subtotal;

    private String tax;
    private BigDecimal taxamount;
    private String discount;
    private BigDecimal discountamount;

    @NotNull
    private BigDecimal total;

    @NotNull
    private BigDecimal paid;

    @NotNull
    private Integer totalitems;

    @NotNull
    private String paidmethod;

    @NotNull
    private Long registerId;
}
