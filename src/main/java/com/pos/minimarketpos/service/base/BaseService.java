package com.pos.minimarketpos.service.base;

import com.pos.minimarketpos.dto.base.BaseDTO;
import com.pos.minimarketpos.exception.ResourceNotFoundException;
import com.pos.minimarketpos.model.base.BaseEntity;
import com.pos.minimarketpos.repository.base.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseService<E extends BaseEntity, D extends BaseDTO> {

    @Autowired
    protected ModelMapper modelMapper;

    protected abstract BaseRepository<E> getRepository();

    private final Class<E> entityClass;
    private final Class<D> dtoClass;

    @SuppressWarnings("unchecked")
    public BaseService() {
        this.entityClass = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.dtoClass = (Class<D>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    // ========== READ OPERATIONS ==========

    @Transactional(readOnly = true)
    public List<D> findAll() {
        log.debug("Finding all {} entities", entityClass.getSimpleName());
        return getRepository().findAllActive().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<D> findAll(Pageable pageable) {
        log.debug("Finding all {} entities with pagination", entityClass.getSimpleName());
        return getRepository().findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public D findById(Long id) {
        log.debug("Finding {} by id: {}", entityClass.getSimpleName(), id);
        E entity = getRepository().findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));
        return convertToDto(entity);
    }

    @Transactional(readOnly = true)
    public List<D> findBySpecification(Specification<E> spec) {
        log.debug("Finding {} by specification", entityClass.getSimpleName());
        return getRepository().findAll(spec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<D> findBySpecification(Specification<E> spec, Pageable pageable) {
        log.debug("Finding {} by specification with pagination", entityClass.getSimpleName());
        return getRepository().findAll(spec, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public long count() {
        return getRepository().count();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return getRepository().existsById(id);
    }

    // ========== CREATE OPERATION ==========

    @Transactional
    public D create(D dto) {
        log.info("Creating new {}", entityClass.getSimpleName());
        E entity = convertToEntity(dto);
        beforeCreate(entity);
        E savedEntity = getRepository().save(entity);
        afterCreate(savedEntity);
        log.info("{} created successfully with id: {}", entityClass.getSimpleName(), savedEntity.getId());
        return convertToDto(savedEntity);
    }

    @Transactional
    public List<D> createAll(List<D> dtos) {
        log.info("Creating {} {} entities", dtos.size(), entityClass.getSimpleName());
        List<E> entities = dtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        entities.forEach(this::beforeCreate);
        List<E> savedEntities = getRepository().saveAll(entities);
        savedEntities.forEach(this::afterCreate);

        return savedEntities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== UPDATE OPERATION ==========

    @Transactional
    public D update(Long id, D dto) {
        log.info("Updating {} with id: {}", entityClass.getSimpleName(), id);
        E existingEntity = getRepository().findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));

        beforeUpdate(existingEntity, dto);
        updateEntityFromDto(existingEntity, dto);
        E updatedEntity = getRepository().save(existingEntity);
        afterUpdate(updatedEntity);

        log.info("{} updated successfully: {}", entityClass.getSimpleName(), id);
        return convertToDto(updatedEntity);
    }

    @Transactional
    public D patch(Long id, D dto) {
        log.info("Patching {} with id: {}", entityClass.getSimpleName(), id);
        E existingEntity = getRepository().findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));

        beforePatch(existingEntity, dto);
        patchEntityFromDto(existingEntity, dto);
        E patchedEntity = getRepository().save(existingEntity);
        afterPatch(patchedEntity);

        return convertToDto(patchedEntity);
    }

    // ========== DELETE OPERATIONS ==========

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting {} with id: {}", entityClass.getSimpleName(), id);
        E entity = getRepository().findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));

        beforeDelete(entity);
        getRepository().delete(entity);
        afterDelete(id);
        log.info("{} deleted successfully: {}", entityClass.getSimpleName(), id);
    }

    @Transactional
    public void softDelete(Long id) {
        log.info("Soft deleting {} with id: {}", entityClass.getSimpleName(), id);
        E entity = getRepository().findByIdActive(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));

        beforeSoftDelete(entity);
        entity.setDeleted(true);
        entity.setUpdatedAt(LocalDateTime.now());
        getRepository().save(entity);
        afterSoftDelete(entity);
        log.info("{} soft deleted successfully: {}", entityClass.getSimpleName(), id);
    }

    @Transactional
    public void restore(Long id) {
        log.info("Restoring {} with id: {}", entityClass.getSimpleName(), id);
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        entityClass.getSimpleName() + " not found with id: " + id));

        entity.setDeleted(false);
        entity.setUpdatedAt(LocalDateTime.now());
        getRepository().save(entity);
        log.info("{} restored successfully: {}", entityClass.getSimpleName(), id);
    }

    // ========== CONVERSION METHODS ==========

    protected D convertToDto(E entity) {
        return modelMapper.map(entity, dtoClass);
    }

    protected E convertToEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }

    protected void updateEntityFromDto(E entity, D dto) {
        modelMapper.map(dto, entity);
    }

    protected void patchEntityFromDto(E entity, D dto) {
        // Override untuk custom patch logic
        modelMapper.map(dto, entity);
    }

    // ========== LIFECYCLE HOOKS ==========
    // Override methods ini untuk custom behavior

    protected void beforeCreate(E entity) {
        // Hook sebelum create
    }

    protected void afterCreate(E entity) {
        // Hook setelah create
    }

    protected void beforeUpdate(E existingEntity, D dto) {
        // Hook sebelum update
    }

    protected void afterUpdate(E entity) {
        // Hook setelah update
    }

    protected void beforePatch(E existingEntity, D dto) {
        // Hook sebelum patch
    }

    protected void afterPatch(E entity) {
        // Hook setelah patch
    }

    protected void beforeDelete(E entity) {
        // Hook sebelum delete
    }

    protected void afterDelete(Long id) {
        // Hook setelah delete
    }

    protected void beforeSoftDelete(E entity) {
        // Hook sebelum soft delete
    }

    protected void afterSoftDelete(E entity) {
        // Hook setelah soft delete
    }

    // ========== VALIDATION ==========

    protected void validateCreate(D dto) {
        // Override untuk custom validation
    }

    protected void validateUpdate(Long id, D dto) {
        // Override untuk custom validation
    }
}
