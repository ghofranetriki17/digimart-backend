package com.nexashop.application.common;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static <T> PageResult<T> of(List<T> items, int page, int size, long totalItems) {
        int resolvedSize = size < 1 ? 1 : size;
        int resolvedPage = Math.max(page, 0);
        int totalPages = (int) Math.ceil(totalItems / (double) resolvedSize);
        boolean hasNext = resolvedPage + 1 < totalPages;
        boolean hasPrevious = resolvedPage > 0;
        return new PageResult<>(items, resolvedPage, resolvedSize, totalItems, totalPages, hasNext, hasPrevious);
    }
}
