package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.CategoryCashflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryCashflowRepository extends JpaRepository<CategoryCashflow, Long> {
}
