package com.besmartmobile.union.app;

import com.besmartmobile.union.lib.Function;

import lombok.NonNull;

public class SomeUnionExt2 {

    public static <T> T match(@NonNull SomeUnion someUnion,
                              @NonNull Function<SomeUnion.Variant1, T> emptyErrorFunction,
                              @NonNull Function<SomeUnion.Variant2, T> serverErrorFunction) {
        if (someUnion instanceof SomeUnion.Variant1) {
            return emptyErrorFunction.apply((SomeUnion.Variant1) someUnion);
        }
        if (someUnion instanceof SomeUnion.Variant2) {
            return serverErrorFunction.apply((SomeUnion.Variant2) someUnion);
        }
        throw new IllegalStateException();
    }

    public static SomeUnion variant1() {
        return new SomeUnion.Variant1();
    }

    public static SomeUnion variant2(@NonNull String message) {
        return new SomeUnion.Variant2(message);
    }
}
