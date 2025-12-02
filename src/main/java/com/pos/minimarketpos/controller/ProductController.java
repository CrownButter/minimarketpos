package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.dto.request.ProductRequest;
import com.pos.minimarketpos.dto.request.StockUpdateRequest;
import com.pos.minimarketpos.dto.response.ProductDTO;
import com.pos.minimarketpos.service.ProductService;
import com.pos.minimarketpos.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String type) {

        List<ProductDTO> products;
        if (supplier != null || type != null) {
            products = productService.getProductsByFilters(supplier, type);
        } else {
            products = productService.getAllProducts();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProductDTO> getProductByCode(@PathVariable String code) {
        return ResponseEntity.ok(productService.getProductByCode(code));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String term) {
        return ResponseEntity.ok(productService.searchProducts(term));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PostMapping("/{id}/stock/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> updateStock(
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {
        request.setProductId(id);
        stockService.updateStock(request);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @PostMapping("/{id}/stock/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<String> addStock(
            @PathVariable Long id,
            @RequestBody StockUpdateRequest request) {
        request.setProductId(id);
        stockService.addStock(request);
        return ResponseEntity.ok("Stock added successfully");
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // Implement file upload logic
        return ResponseEntity.ok("Image uploaded successfully");
    }
}
