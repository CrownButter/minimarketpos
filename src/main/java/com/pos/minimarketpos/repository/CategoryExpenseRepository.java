package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.CategoryExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryExpenseRepository extends JpaRepository<CategoryExpense, Long> {
}
