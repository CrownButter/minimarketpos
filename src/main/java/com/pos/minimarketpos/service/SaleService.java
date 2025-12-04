package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.request.SaleRequest;
import com.pos.minimarketpos.dto.response.SaleDTO;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Register;
import com.pos.minimarketpos.model.Sale;
import com.pos.minimarketpos.model.SaleItem;
import com.pos.minimarketpos.repository.RegisterRepository;
import com.pos.minimarketpos.repository.SaleItemRepository;
import com.pos.minimarketpos.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final RegisterRepository registerRepository; // 1. Inject RegisterRepository
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<SaleDTO> getAllSales() {
        return saleRepository.findAll().stream()
                .map(sale -> modelMapper.map(sale, SaleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SaleDTO getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return modelMapper.map(sale, SaleDTO.class);
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByRegister(Long registerId) {
        return saleRepository.findByRegisterId(registerId).stream()
                .map(sale -> modelMapper.map(sale, SaleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByClient(Long clientId) {
        return saleRepository.findByClientId(clientId).stream()
                .map(sale -> modelMapper.map(sale, SaleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end).stream()
                .map(sale -> modelMapper.map(sale, SaleDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public SaleDTO createSale(SaleRequest request, String username) {
        // 2. Fetch Register Entity dari Database
        Register register = registerRepository.findById(request.getRegisterId())
                .orElseThrow(() -> new ResourceNotFoundException("Register not found with id: " + request.getRegisterId()));

        Sale sale = Sale.builder()
                .clientId(request.getClientId())
                .clientname(request.getClientname())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .taxamount(request.getTaxamount())
                .discount(request.getDiscount())
                .discountamount(request.getDiscountamount())
                .total(request.getTotal())
                .paid(request.getPaid())
                .firstpayement(request.getPaid())
                .totalitems(request.getTotalitems())
                .paidmethod(request.getPaidmethod())

                // 3. Masukkan Object Register (Bukan ID long)
                .register(register)

                .createdBy(username)
                .status(request.getPaid().compareTo(request.getTotal()) >= 0 ? 0 : 1)
                .build();

        Sale savedSale = saleRepository.save(sale);
        return modelMapper.map(savedSale, SaleDTO.class);
    }

    @Transactional
    public SaleDTO updateSale(Long id, SaleRequest request) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        sale.setClientId(request.getClientId());
        sale.setClientname(request.getClientname());
        sale.setSubtotal(request.getSubtotal());
        sale.setTax(request.getTax());
        sale.setTaxamount(request.getTaxamount());
        sale.setDiscount(request.getDiscount());
        sale.setDiscountamount(request.getDiscountamount());
        sale.setTotal(request.getTotal());
        sale.setPaid(request.getPaid());
        sale.setTotalitems(request.getTotalitems());
        sale.setPaidmethod(request.getPaidmethod());
        sale.setModifiedAt(LocalDateTime.now());

        // Note: Biasanya Register tidak berubah saat update sale, jadi tidak perlu di-set ulang

        Sale updatedSale = saleRepository.save(sale);
        return modelMapper.map(updatedSale, SaleDTO.class);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        // Delete associated sale items
        saleItemRepository.deleteAll(saleItemRepository.findBySaleId(id));
        saleRepository.delete(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleItem> getSaleItems(Long saleId) {
        return saleItemRepository.findBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodayTotal() {
        BigDecimal total = saleRepository.getTodayTotal();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodayProfit() {
        BigDecimal profit = saleRepository.getTodayProfit();
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByStatus(Integer status) {
        return saleRepository.findByStatus(status).stream()
                .map(sale -> modelMapper.map(sale, SaleDTO.class))
                .collect(Collectors.toList());
    }
}