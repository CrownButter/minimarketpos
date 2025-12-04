package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.request.StoreRequest;
import com.pos.minimarketpos.dto.response.StoreDTO;
import com.pos.minimarketpos.dto.response.StoreStatisticsDTO;
import com.pos.minimarketpos.model.Store;
import com.pos.minimarketpos.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/active")
    public ResponseEntity<List<StoreDTO>> getActiveStores() {
        return ResponseEntity.ok(storeService.getActiveStores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @PostMapping
    public ResponseEntity<StoreDTO> createStore(@RequestBody @Valid StoreRequest request) {
        // Manual Mapping DTO -> Entity
        Store store = new Store();
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setFooterText(request.getFooterText());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeService.createStore(store));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDTO> updateStore(
            @PathVariable Long id,
            @RequestBody @Valid StoreRequest request) {

        Store store = new Store();
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setFooterText(request.getFooterText());

        return ResponseEntity.ok(storeService.updateStore(id, store));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateStore(@PathVariable Long id) {
        storeService.deactivateStore(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<StoreDTO> activateStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.activateStore(id));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<StoreStatisticsDTO> getStoreStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreStatistics(id));
    }
}