package com.nexashop.application.common;

public record PageRequest(int page, int size) {

    public static PageRequest of(int page, int size) {
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = size < 1 ? 20 : size;
        return new PageRequest(resolvedPage, resolvedSize);
    }
}
