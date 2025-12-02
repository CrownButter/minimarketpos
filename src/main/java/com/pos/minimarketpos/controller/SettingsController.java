package com.pos.minimarketpos.controller;

import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Setting;
import com.pos.minimarketpos.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    private final SettingRepository settingRepository;

    @GetMapping
    public ResponseEntity<List<Setting>> getAllSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Setting> getSettingById(@PathVariable Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));
        return ResponseEntity.ok(setting);
    }

    @GetMapping("/default")
    public ResponseEntity<Setting> getDefaultSettings() {
        List<Setting> settings = settingRepository.findAll();
        if (settings.isEmpty()) {
            // Return default settings
            Setting defaultSetting = Setting.builder()
                    .companyname("My Store")
                    .currency("USD")
                    .decimals(2)
                    .timezone("UTC")
                    .language("en")
                    .build();
            return ResponseEntity.ok(defaultSetting);
        }
        return ResponseEntity.ok(settings.get(0));
    }

    @PostMapping
    public ResponseEntity<Setting> createSetting(@RequestBody Setting setting) {
        Setting savedSetting = settingRepository.save(setting);
        return ResponseEntity.ok(savedSetting);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Setting> updateSetting(
            @PathVariable Long id,
            @RequestBody Setting settingDetails) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));

        setting.setCompanyname(settingDetails.getCompanyname());
        setting.setPhone(settingDetails.getPhone());
        setting.setEmail(settingDetails.getEmail());
        setting.setLogo(settingDetails.getLogo());
        setting.setCurrency(settingDetails.getCurrency());
        setting.setDecimals(settingDetails.getDecimals());
        setting.setTimezone(settingDetails.getTimezone());
        setting.setLanguage(settingDetails.getLanguage());
        setting.setReceiptheader(settingDetails.getReceiptheader());
        setting.setReceiptfooter(settingDetails.getReceiptfooter());
        setting.setStripeSecretKey(settingDetails.getStripeSecretKey());
        setting.setStripePublicKey(settingDetails.getStripePublicKey());

        Setting updatedSetting = settingRepository.save(setting);
        return ResponseEntity.ok(updatedSetting);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSetting(@PathVariable Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with id: " + id));
        settingRepository.delete(setting);
        return ResponseEntity.ok("Setting deleted successfully");
    }
}