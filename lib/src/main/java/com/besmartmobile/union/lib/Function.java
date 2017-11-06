package com.besmartmobile.union.lib;

@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
