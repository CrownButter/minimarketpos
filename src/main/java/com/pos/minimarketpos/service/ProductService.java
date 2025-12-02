package com.pos.minimarketpos.service;

import com.pos.minimarketpos.dto.request.ProductRequest;
import com.pos.minimarketpos.dto.response.ProductDTO;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.Product;
import com.pos.minimarketpos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByFilters(String supplier, String type) {
        return productRepository.findByFilters(supplier, type).stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductByCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + code));
        return modelMapper.map(product, ProductDTO.class);
    }

    @Transactional
    public ProductDTO createProduct(ProductRequest request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }

        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .category(request.getCategory())
                .cost(request.getCost())
                .price(request.getPrice())
                .description(request.getDescription())
                .tax(request.getTax())
                .taxmethod(request.getTaxmethod())
                .alertqt(request.getAlertqt())
                .color(request.getColor())
                .supplier(request.getSupplier())
                .unit(request.getUnit())
                .type(request.getType())
                .createdAt(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setCost(request.getCost());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setTax(request.getTax());
        product.setTaxmethod(request.getTaxmethod());
        product.setAlertqt(request.getAlertqt());
        product.setColor(request.getColor());
        product.setSupplier(request.getSupplier());
        product.setUnit(request.getUnit());
        product.setType(request.getType());
        product.setModifiedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String term) {
        return productRepository.searchProducts(term).stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }
}
