package com.pos.minimarketpos.repository;

import com.pos.minimarketpos.model.Hold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldRepository extends JpaRepository<Hold, Long> {
    List<Hold> findByRegisterIdOrderByNumberAsc(Long registerId);
    Optional<Hold> findByNumberAndRegisterId(Integer number, Long registerId);
    Optional<Hold> findFirstByRegisterIdOrderByNumberDesc(Long registerId);
    void deleteByRegisterId(Long registerId);
}
