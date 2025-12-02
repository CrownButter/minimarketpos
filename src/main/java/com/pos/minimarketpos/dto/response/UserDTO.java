package com.pos.minimarketpos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;
    private Boolean isActive;
}
