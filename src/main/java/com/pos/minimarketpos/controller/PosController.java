package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.request.SaleRequest;
import com.pos.minimarketpos.dto.response.PosaleDTO;
import com.pos.minimarketpos.service.PosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class PosController {

    private final PosService posService;

    @PostMapping("/add-product")
    public ResponseEntity<Map<String, Object>> addProductToCart(
            @RequestBody PosaleDTO posaleDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        var result = posService.addProductToCart(posaleDTO, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/load-cart")
    public ResponseEntity<String> loadCartItems(@RequestParam Long registerId) {
        String htmlContent = posService.loadPosales(registerId);
        return ResponseEntity.ok(htmlContent);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteCartItem(@PathVariable Long id) {
        posService.deletePosale(id);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Map<String, Object>> editCartItem(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {

        var result = posService.editPosale(id, request.get("qt"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/subtotal")
    public ResponseEntity<String> getSubtotal(@RequestParam Long registerId) {
        String subtotal = posService.calculateSubtotal(registerId);
        return ResponseEntity.ok(subtotal);
    }

    @GetMapping("/total-items")
    public ResponseEntity<Integer> getTotalItems(@RequestParam Long registerId) {
        int total = posService.getTotalItems(registerId);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Boolean>> resetPos(@RequestParam Long registerId) {
        posService.resetPos(registerId);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @PostMapping("/complete-sale")
    public ResponseEntity<String> completeSale(
            @RequestBody SaleRequest saleRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        String receipt = posService.completeSale(saleRequest, userDetails.getUsername());
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/discount/{customerId}")
    public ResponseEntity<Map<String, String>> getCustomerDiscount(@PathVariable Long customerId) {
        var discount = posService.getCustomerDiscount(customerId);
        return ResponseEntity.ok(discount);
    }

    // Hold functionality
    @GetMapping("/holds/{registerId}")
    public ResponseEntity<String> getHoldList(@PathVariable Long registerId) {
        String holdList = posService.getHoldList(registerId);
        return ResponseEntity.ok(holdList);
    }

    @PostMapping("/hold/add")
    public ResponseEntity<Map<String, Boolean>> addHold(@RequestParam Long registerId) {
        posService.addHold(registerId);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @DeleteMapping("/hold/remove")
    public ResponseEntity<Map<String, Boolean>> removeHold(
            @RequestParam Long registerId,
            @RequestParam Integer number) {

        posService.removeHold(registerId, number);
        return ResponseEntity.ok(Map.of("status", true));
    }

    @PostMapping("/hold/select")
    public ResponseEntity<Map<String, Boolean>> selectHold(
            @RequestParam Long registerId,
            @RequestParam Integer number) {

        posService.selectHold(registerId, number);
        return ResponseEntity.ok(Map.of("status", true));
    }
}