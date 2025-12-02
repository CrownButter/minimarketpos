package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
    Boolean existsByCode(String code);
    List<Product> findByCategory(String category);
    List<Product> findBySupplier(String supplier);
    List<Product> findByType(String type);

    @Query("SELECT p FROM Product p WHERE " +
            "(:supplier IS NULL OR :supplier = '99' OR p.supplier = :supplier) AND " +
            "(:type IS NULL OR :type = '99' OR p.type = :type)")
    List<Product> findByFilters(@Param("supplier") String supplier, @Param("type") String type);

    @Query("SELECT p FROM Product p WHERE " +
            "p.name LIKE %:term% OR p.code LIKE %:term%")
    List<Product> searchProducts(@Param("term") String term);
}
