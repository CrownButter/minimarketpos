package com.pos.minimarketpos.util;

import com.pos.minimarketpos.model.base.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T extends BaseEntity> {

    private final List<Specification<T>> specifications = new ArrayList<>();

    public SpecificationBuilder<T> with(String key, String operation, Object value) {
        if (value == null) return this;

        specifications.add((root, query, criteriaBuilder) -> {
            switch (operation) {
                case "=":
                    return criteriaBuilder.equal(root.get(key), value);
                case "!=":
                    return criteriaBuilder.notEqual(root.get(key), value);
                case ">":
                    return criteriaBuilder.greaterThan(root.get(key), (Comparable) value);
                case "<":
                    return criteriaBuilder.lessThan(root.get(key), (Comparable) value);
                case ">=":
                    return criteriaBuilder.greaterThanOrEqualTo(root.get(key), (Comparable) value);
                case "<=":
                    return criteriaBuilder.lessThanOrEqualTo(root.get(key), (Comparable) value);
                case "LIKE":
                    return criteriaBuilder.like(root.get(key), "%" + value + "%");
                case "IN":
                    return root.get(key).in((List<?>) value);
                default:
                    return null;
            }
        });
        return this;
    }

    public SpecificationBuilder<T> between(String key, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(key), start, end));
        }
        return this;
    }

    public SpecificationBuilder<T> between(String key, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(key), start, end));
        }
        return this;
    }

    public Specification<T> build() {
        if (specifications.isEmpty()) {
            return null;
        }

        Specification<T> result = specifications.get(0);
        for (int i = 1; i < specifications.size(); i++) {
            result = Specification.where(result).and(specifications.get(i));
        }
        return result;
    }
}