package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.response.RegisterDTO;
import com.pos.minimarketpos.model.User;
import com.pos.minimarketpos.service.RegisterService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/registers")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    // 1. Endpoint untuk Membuka Kasir (Wajib dilakukan sebelum transaksi)
    @PostMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'MANAGER')")
    public ResponseEntity<RegisterDTO> openRegister(
            @RequestBody OpenRegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Mengambil ID user yang sedang login
        // Casting ke User model custom kita untuk mendapatkan ID
        Long userId = ((User) userDetails).getId();

        RegisterDTO register = registerService.openRegister(
                userId,
                request.getStoreId(),
                request.getCashInhand()
        );

        return ResponseEntity.ok(register);
    }

    // 2. Endpoint untuk Menutup Kasir
    @PostMapping("/close/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'MANAGER')")
    public ResponseEntity<RegisterDTO> closeRegister(
            @PathVariable Long id,
            @RequestBody CloseRegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = ((User) userDetails).getId();

        RegisterDTO register = registerService.closeRegister(
                id,
                userId,
                request.getNote()
        );

        return ResponseEntity.ok(register);
    }

    // 3. Endpoint untuk melihat Register user saat ini
    @GetMapping("/my")
    public ResponseEntity<List<RegisterDTO>> getMyRegisters(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = ((User) userDetails).getId();
        return ResponseEntity.ok(registerService.getRegistersByUser(userId));
    }

    // 4. Endpoint untuk melihat status Register di Toko tertentu
    @GetMapping("/store/{storeId}/open")
    public ResponseEntity<RegisterDTO> getOpenRegister(@PathVariable Long storeId) {
        RegisterDTO register = registerService.getOpenRegister(storeId);
        if (register == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(register);
    }

    // --- Inner Classes untuk Request Body ---

    @Data
    public static class OpenRegisterRequest {
        private Long storeId;
        private BigDecimal cashInhand;
    }

    @Data
    public static class CloseRegisterRequest {
        private String note;
    }
}