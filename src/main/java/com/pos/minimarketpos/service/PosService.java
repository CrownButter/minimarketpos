package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.request.SaleRequest;
import com.pos.minimarketpos.dto.response.PosaleDTO;
import com.pos.minimarketpos.exception.InsufficientStockException;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.*;
import com.pos.minimarketpos.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PosService {

    private final PosaleRepository posaleRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final RegisterRepository registerRepository;
    private final CustomerRepository customerRepository;
    private final HoldRepository holdRepository;
    private final ModelMapper modelMapper;
    private final StockService stockService;

    @Transactional
    public Map<String, Object> addProductToCart(PosaleDTO posaleDTO, String username) {
        Map<String, Object> response = new HashMap<>();

        // Check if product already exists in cart
        Optional<PosSale> existingPosale = posaleRepository
                .findByRegisterIdAndProductIdAndStatus(
                        posaleDTO.getRegisterId(),
                        posaleDTO.getProductId(),
                        0
                );

        if (existingPosale.isPresent()) {
            PosSale posale = existingPosale.get();
            posale.setQt(posale.getQt() + 1);
            posaleRepository.save(posale);
            response.put("status", true);
            response.put("message", "Product quantity updated");
        } else {
            Product product = productRepository.findById(posaleDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            PosSale posale = PosSale.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .cost(product.getCost())
                    .price(product.getPrice())
                    .number(0)
                    .registerId(posaleDTO.getRegisterId())
                    .qt(1)
                    .status(0)
                    .build();

            posaleRepository.save(posale);
            response.put("status", true);
            response.put("message", "Product added to cart");
        }

        return response;
    }

    @Transactional(readOnly = true)
    public String loadPosales(Long registerId) {
        List<PosSale> posales = posaleRepository.findByRegisterIdAndStatus(registerId, 0);

        StringBuilder html = new StringBuilder();
        for (PosSale posale : posales) {
            html.append("<div class='cart-item' data-id='").append(posale.getId()).append("'>");
            html.append("<span>").append(posale.getName()).append("</span>");
            html.append("<span>").append(posale.getQt()).append("</span>");
            html.append("<span>").append(posale.getPrice()).append("</span>");
            html.append("<button onclick='deleteItem(").append(posale.getId()).append(")'>Delete</button>");
            html.append("</div>");
        }

        return html.toString();
    }

    @Transactional
    public void deletePosale(Long id) {
        posaleRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> editPosale(Long id, Integer quantity) {
        PosSale posale = posaleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        posale.setQt(quantity);
        posaleRepository.save(posale);

        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("subtotal", posale.getPrice().multiply(new BigDecimal(quantity)));
        return response;
    }

    @Transactional(readOnly = true)
    public String calculateSubtotal(Long registerId) {
        List<PosSale> posales = posaleRepository.findByRegisterIdAndStatus(registerId, 0);
        BigDecimal subtotal = posales.stream()
                .map(p -> p.getPrice().multiply(new BigDecimal(p.getQt())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotal.toString();
    }

    @Transactional(readOnly = true)
    public int getTotalItems(Long registerId) {
        List<PosSale> posales = posaleRepository.findByRegisterIdAndStatus(registerId, 0);
        return posales.stream().mapToInt(PosSale::getQt).sum();
    }

    @Transactional
    public void resetPos(Long registerId) {
        posaleRepository.deleteByRegisterIdAndStatus(registerId, 0);
    }

    @Transactional
    public String completeSale(SaleRequest request, String username) {
        // Get cart items
        List<PosSale> cartItems = posaleRepository.findByRegisterIdAndStatus(request.getRegisterId(), 0);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Get register to find store
        Register register = registerRepository.findById(request.getRegisterId())
                .orElseThrow(() -> new ResourceNotFoundException("Register not found"));

        // Calculate costs
        BigDecimal totalCost = cartItems.stream()
                .map(item -> item.getCost().multiply(new BigDecimal(item.getQt())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create sale
        Sale sale = Sale.builder()
                .clientId(request.getClientId())
                .clientname(request.getClientname())
                .cost(totalCost)
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
                .registerId(request.getRegisterId())
                .createdBy(username)
                .status(request.getPaid().compareTo(request.getTotal()) >= 0 ? 0 : 1)
                .build();

        Sale savedSale = saleRepository.save(sale);

        // Create sale items and reduce stock
        for (PosSale cartItem : cartItems) {
            SaleItem saleItem = SaleItem.builder()
                    .productId(cartItem.getProductId())
                    .name(cartItem.getName())
                    .price(cartItem.getPrice())
                    .qt(cartItem.getQt())
                    .subtotal(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQt())))
                    .saleId(savedSale.getId())
                    .date(LocalDateTime.now())
                    .build();

            saleItemRepository.save(saleItem);

            // Reduce stock
            try {
                stockService.reduceStock(register.getStoreId(), cartItem.getProductId(), cartItem.getQt());
            } catch (InsufficientStockException e) {
                throw new InsufficientStockException("Insufficient stock for product: " + cartItem.getName());
            }
        }

        // Update register totals
        updateRegisterTotals(register, request);

        // Clear cart
        posaleRepository.deleteByRegisterIdAndStatus(request.getRegisterId(), 0);

        // Generate receipt
        return generateReceipt(savedSale, cartItems);
    }

    private void updateRegisterTotals(Register register, SaleRequest request) {
        switch (request.getPaidmethod().toLowerCase()) {
            case "cash":
                register.setCashTotal(register.getCashTotal().add(request.getTotal()));
                register.setCashSub(register.getCashSub().add(request.getTotal()));
                break;
            case "card":
                register.setCcTotal(register.getCcTotal().add(request.getTotal()));
                register.setCcSub(register.getCcSub().add(request.getTotal()));
                break;
            case "cheque":
                register.setChequeTotal(register.getChequeTotal().add(request.getTotal()));
                register.setChequeSub(register.getChequeSub().add(request.getTotal()));
                break;
        }
        registerRepository.save(register);
    }

    private String generateReceipt(Sale sale, List<PosSale> items) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== RECEIPT =====\n");
        receipt.append("Sale ID: ").append(sale.getId()).append("\n");
        receipt.append("Date: ").append(sale.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        receipt.append("==================\n\n");

        for (PosSale item : items) {
            receipt.append(item.getName()).append("\n");
            receipt.append("  ").append(item.getQt()).append(" x ")
                    .append(item.getPrice()).append(" = ")
                    .append(item.getPrice().multiply(new BigDecimal(item.getQt()))).append("\n");
        }

        receipt.append("\n==================\n");
        receipt.append("Subtotal: ").append(sale.getSubtotal()).append("\n");
        if (sale.getTaxamount() != null && sale.getTaxamount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append("Tax: ").append(sale.getTaxamount()).append("\n");
        }
        if (sale.getDiscountamount() != null && sale.getDiscountamount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append("Discount: ").append(sale.getDiscountamount()).append("\n");
        }
        receipt.append("TOTAL: ").append(sale.getTotal()).append("\n");
        receipt.append("Paid: ").append(sale.getPaid()).append("\n");
        BigDecimal change = sale.getPaid().subtract(sale.getTotal());
        if (change.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append("Change: ").append(change).append("\n");
        }
        receipt.append("==================\n");
        receipt.append("\nThank you for your purchase!\n");

        return receipt.toString();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getCustomerDiscount(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Map<String, String> response = new HashMap<>();
        response.put("discount", customer.getDiscount() != null ? customer.getDiscount() : "0");
        return response;
    }

    // Hold operations
    @Transactional(readOnly = true)
    public String getHoldList(Long registerId) {
        List<Hold> holds = holdRepository.findByRegisterIdOrderByNumberAsc(registerId);

        StringBuilder html = new StringBuilder();
        for (Hold hold : holds) {
            html.append("<div class='hold-item' data-number='").append(hold.getNumber()).append("'>");
            html.append("<span>Hold #").append(hold.getNumber()).append("</span>");
            html.append("<span>").append(hold.getTime()).append("</span>");
            html.append("<button onclick='selectHold(").append(hold.getNumber()).append(")'>Load</button>");
            html.append("<button onclick='removeHold(").append(hold.getNumber()).append(")'>Delete</button>");
            html.append("</div>");
        }

        return html.toString();
    }

    @Transactional
    public void addHold(Long registerId) {
        List<PosSale> cartItems = posaleRepository.findByRegisterIdAndStatus(registerId, 0);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Get next hold number
        Optional<Hold> lastHold = holdRepository.findFirstByRegisterIdOrderByNumberDesc(registerId);
        int nextNumber = lastHold.map(h -> h.getNumber() + 1).orElse(1);

        // Create hold
        Hold hold = Hold.builder()
                .number(nextNumber)
                .time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .registerId(registerId)
                .build();

        holdRepository.save(hold);

        // Update cart items with hold number
        for (PosSale item : cartItems) {
            item.setNumber(nextNumber);
            item.setStatus(1); // Mark as held
            posaleRepository.save(item);
        }
    }

    @Transactional
    public void removeHold(Long registerId, Integer number) {
        posaleRepository.deleteByNumberAndRegisterId(number, registerId);
        holdRepository.findByNumberAndRegisterId(number, registerId)
                .ifPresent(holdRepository::delete);
    }

    @Transactional
    public void selectHold(Long registerId, Integer number) {
        // Clear current cart
        posaleRepository.deleteByRegisterIdAndStatus(registerId, 0);

        // Load held items
        List<PosSale> heldItems = posaleRepository.findByNumberAndRegisterId(number, registerId);
        for (PosSale item : heldItems) {
            item.setStatus(0); // Mark as active
            item.setNumber(0);
            posaleRepository.save(item);
        }

        // Remove hold
        holdRepository.findByNumberAndRegisterId(number, registerId)
                .ifPresent(holdRepository::delete);
    }
}