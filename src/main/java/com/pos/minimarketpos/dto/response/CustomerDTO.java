package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String discount;
    private LocalDateTime createdAt;
}
