package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboItemRepository extends JpaRepository<ComboItem, Long> {
    List<ComboItem> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}
