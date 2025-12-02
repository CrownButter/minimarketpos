package com.pos.minimarketpos.controller.base;

import com.pos.minimarketpos.dto.base.BaseDTO;
import com.pos.minimarketpos.model.base.BaseEntity;
import com.pos.minimarketpos.service.base.BaseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseController<E extends BaseEntity, D extends BaseDTO> {

    protected abstract BaseService<E, D> getService();

    // ========== READ ENDPOINTS ==========

    @GetMapping
    public ResponseEntity<List<D>> getAll() {
        List<D> result = getService().findAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<D>> getAllPaginated(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<D> result = getService().findAll(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<D> getById(@PathVariable Long id) {
        D result = getService().findById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        long count = getService().count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable Long id) {
        boolean exists = getService().existsById(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // ========== CREATE ENDPOINTS ==========

    @PostMapping
    public ResponseEntity<D> create(@Valid @RequestBody D dto) {
        D created = getService().create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<D>> createBatch(@Valid @RequestBody List<D> dtos) {
        List<D> created = getService().createAll(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========== UPDATE ENDPOINTS ==========

    @PutMapping("/{id}")
    public ResponseEntity<D> update(@PathVariable Long id, @Valid @RequestBody D dto) {
        D updated = getService().update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<D> patch(@PathVariable Long id, @RequestBody D dto) {
        D patched = getService().patch(id, dto);
        return ResponseEntity.ok(patched);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        getService().deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Resource deleted successfully");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/soft-delete")
    public ResponseEntity<Map<String, String>> softDelete(@PathVariable Long id) {
        getService().softDelete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Resource soft deleted successfully");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, String>> restore(@PathVariable Long id) {
        getService().restore(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Resource restored successfully");
        response.put("id", id.toString());
        return ResponseEntity.ok(response);
    }
}