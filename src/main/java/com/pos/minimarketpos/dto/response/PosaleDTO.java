package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PosaleDTO {
    private Long id;
    private Long productId;
    private String name;
    private BigDecimal cost;
    private BigDecimal price;
    private Integer number;
    private Long registerId;
    private Integer qt;
    private Integer status;
}
