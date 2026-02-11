package com.nexashop.api.dto.response;

import com.nexashop.application.common.PageResult;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static <T, R> PageResponse<R> from(PageResult<T> result, Function<T, R> mapper) {
        List<R> mapped = result.items().stream().map(mapper).toList();
        return new PageResponse<>(
                mapped,
                result.page(),
                result.size(),
                result.totalItems(),
                result.totalPages(),
                result.hasNext(),
                result.hasPrevious()
        );
    }
}
