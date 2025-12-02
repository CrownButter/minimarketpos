package com.pos.minimarketpos.repository.base;

import com.pos.minimarketpos.model.base.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllActive();

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findByIdActive(@Param("id") Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();
}