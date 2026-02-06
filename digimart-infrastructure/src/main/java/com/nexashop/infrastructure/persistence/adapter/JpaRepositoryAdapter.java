package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.CrudRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class JpaRepositoryAdapter<D, E, ID> implements CrudRepositoryPort<D, ID> {

    private final JpaRepository<E, ID> repository;

    protected JpaRepositoryAdapter(JpaRepository<E, ID> repository) {
        this.repository = repository;
    }

    protected abstract E toJpa(D domain);

    protected abstract D toDomain(E entity);

    protected List<D> toDomainList(List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        List<D> results = new ArrayList<>(entities.size());
        for (E entity : entities) {
            results.add(toDomain(entity));
        }
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends D> S save(S entity) {
        E saved = repository.save(toJpa(entity));
        return (S) toDomain(saved);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends D> List<S> saveAll(Iterable<S> entities) {
        List<E> toSave = new ArrayList<>();
        if (entities != null) {
            for (S entity : entities) {
                toSave.add(toJpa(entity));
            }
        }
        List<E> saved = repository.saveAll(toSave);
        List<S> results = new ArrayList<>(saved.size());
        for (E entity : saved) {
            results.add((S) toDomain(entity));
        }
        return results;
    }

    @Override
    public Optional<D> findById(ID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<D> findAll() {
        return toDomainList(repository.findAll());
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public void delete(D entity) {
        repository.delete(toJpa(entity));
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<? extends D> entities) {
        List<E> toDelete = new ArrayList<>();
        if (entities != null) {
            for (D entity : entities) {
                toDelete.add(toJpa(entity));
            }
        }
        repository.deleteAll(toDelete);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
