package com.pos.minimarketpos.repository;
import com.pos.minimarketpos.model.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosaleRepository extends JpaRepository<PosSale, Long> {
    List<PosSale> findByRegisterIdAndStatus(Long registerId, Integer status);
    Optional<PosSale> findByRegisterIdAndProductIdAndStatus(Long registerId, Long productId, Integer status);
    List<PosSale> findByNumberAndRegisterId(Integer number, Long registerId);
    void deleteByRegisterIdAndStatus(Long registerId, Integer status);
    void deleteByNumberAndRegisterId(Integer number, Long registerId);
}
