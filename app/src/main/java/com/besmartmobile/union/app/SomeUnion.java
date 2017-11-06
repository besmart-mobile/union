package com.besmartmobile.union.app;

import com.besmartmobile.union.lib.annotations.UnionAnnotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@UnionAnnotation
public abstract class SomeUnion {

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Variant1 extends SomeUnion {
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Variant2 extends SomeUnion {
        @Getter @NonNull private String message;
    }
}
