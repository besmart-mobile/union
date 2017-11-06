package com.besmartmobile.union.app.gson;


import com.besmartmobile.union.lib.UnionWithClassInfo;
import com.besmartmobile.union.lib.annotations.UnionAnnotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@UnionAnnotation
public abstract class TestUnion extends UnionWithClassInfo {

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Variant1 extends TestUnion {
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Variant2 extends TestUnion {
        @Getter @NonNull private String message;
        @Getter @NonNull private int number;
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Variant3 extends TestUnion {
        @Getter @NonNull private String message;
        @Getter @NonNull private int number;
    }
}
