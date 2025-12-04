package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.response.RegisterDTO;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Register;
import com.pos.minimarketpos.model.Store;
import com.pos.minimarketpos.repository.RegisterRepository;
import com.pos.minimarketpos.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final RegisterRepository registerRepository;
    private final StoreRepository storeRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<RegisterDTO> getAllRegisters() {
        return registerRepository.findAll().stream()
                .map(register -> modelMapper.map(register, RegisterDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegisterDTO getRegisterById(Long id) {
        Register register = registerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + id));
        return modelMapper.map(register, RegisterDTO.class);
    }

    @Transactional(readOnly = true)
    public List<RegisterDTO> getRegistersByUser(Long userId) {
        return registerRepository.findByUserId(userId).stream()
                .map(register -> modelMapper.map(register, RegisterDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegisterDTO> getRegistersByStore(Long storeId) {
        // JPA cukup pintar untuk mapping storeId ke store.id secara otomatis di repository
        return registerRepository.findByStoreId(storeId).stream()
                .map(register -> modelMapper.map(register, RegisterDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegisterDTO getOpenRegister(Long storeId) {
        Optional<Register> register = registerRepository.findByStoreIdAndStatus(storeId, 1);
        return register.map(r -> modelMapper.map(r, RegisterDTO.class)).orElse(null);
    }

    @Transactional
    public RegisterDTO openRegister(Long userId, Long storeId, BigDecimal cashInhand) {
        // 1. Cek apakah ada register yang masih terbuka
        Optional<Register> existingRegister = registerRepository.findByStoreIdAndStatus(storeId, 1);
        if (existingRegister.isPresent()) {
            throw new IllegalArgumentException("A register is already open for this store");
        }

        // 2. Fetch Entity Store (WAJIB dilakukan karena perubahan relasi Entity)
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + storeId));

        // 3. Build Register menggunakan Object Store
        Register register = Register.builder()
                .userId(userId)
                .store(store) // FIX: Menggunakan objek Store, bukan storeId(Long)
                .cashInhand(cashInhand)
                .cashTotal(BigDecimal.ZERO)
                .cashSub(BigDecimal.ZERO)
                .ccTotal(BigDecimal.ZERO)
                .ccSub(BigDecimal.ZERO)
                .chequeTotal(BigDecimal.ZERO)
                .chequeSub(BigDecimal.ZERO)
                .date(LocalDateTime.now())
                .status(1) // 1 = Open
                .build();

        Register savedRegister = registerRepository.save(register);
        return modelMapper.map(savedRegister, RegisterDTO.class);
    }

    @Transactional
    public RegisterDTO closeRegister(Long id, Long closedBy, String note) {
        Register register = registerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + id));

        if (register.getStatus() == 0) {
            throw new IllegalArgumentException("Register is already closed");
        }

        register.setStatus(0); // 0 = Closed
        register.setClosedBy(closedBy);
        register.setClosedAt(LocalDateTime.now());
        register.setNote(note);

        Register closedRegister = registerRepository.save(register);
        return modelMapper.map(closedRegister, RegisterDTO.class);
    }

    @Transactional
    public RegisterDTO updateRegisterTotals(Long id, BigDecimal cashTotal, BigDecimal ccTotal, BigDecimal chequeTotal) {
        Register register = registerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + id));

        if (cashTotal != null) {
            register.setCashTotal(register.getCashTotal().add(cashTotal));
            register.setCashSub(register.getCashSub().add(cashTotal));
        }
        if (ccTotal != null) {
            register.setCcTotal(register.getCcTotal().add(ccTotal));
            register.setCcSub(register.getCcSub().add(ccTotal));
        }
        if (chequeTotal != null) {
            register.setChequeTotal(register.getChequeTotal().add(chequeTotal));
            register.setChequeSub(register.getChequeSub().add(chequeTotal));
        }

        Register updatedRegister = registerRepository.save(register);
        return modelMapper.map(updatedRegister, RegisterDTO.class);
    }

    @Transactional(readOnly = true)
    public List<RegisterDTO> getRegistersByDateRange(Long storeId, LocalDateTime start, LocalDateTime end) {
        return registerRepository.findByStoreAndDateRange(storeId, start, end).stream()
                .map(register -> modelMapper.map(register, RegisterDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRegister(Long id) {
        Register register = registerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + id));

        if (register.getStatus() == 1) {
            throw new IllegalArgumentException("Cannot delete an open register. Close it first.");
        }

        registerRepository.delete(register);
    }

    @Transactional(readOnly = true)
    public BigDecimal getRegisterBalance(Long id) {
        Register register = registerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + id));

        // Mencegah NullPointerException jika nilai awal null (opsional, tapi aman)
        BigDecimal cash = register.getCashInhand() != null ? register.getCashInhand() : BigDecimal.ZERO;
        BigDecimal cashTotal = register.getCashTotal() != null ? register.getCashTotal() : BigDecimal.ZERO;
        BigDecimal cc = register.getCcTotal() != null ? register.getCcTotal() : BigDecimal.ZERO;
        BigDecimal cheque = register.getChequeTotal() != null ? register.getChequeTotal() : BigDecimal.ZERO;

        return cash.add(cashTotal).add(cc).add(cheque);
    }
}