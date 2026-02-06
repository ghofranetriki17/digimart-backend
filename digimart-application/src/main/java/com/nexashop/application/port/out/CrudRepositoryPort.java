package com.nexashop.application.port.out;

import java.util.List;
import java.util.Optional;

public interface CrudRepositoryPort<T, ID> {

    <S extends T> S save(S entity);

    <S extends T> List<S> saveAll(Iterable<S> entities);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    void delete(T entity);

    void deleteById(ID id);

    void deleteAll(Iterable<? extends T> entities);

    long count();
}
