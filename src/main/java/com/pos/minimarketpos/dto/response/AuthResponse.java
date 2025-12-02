package com.pos.minimarketpos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;

    // FIXED: Added @Builder.Default to fix Lombok warning
    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String username;
    private String email;
    private String role;
}