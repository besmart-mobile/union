package com.besmartmobile.union.app.package1;

import com.annimon.stream.function.Function;
import com.besmartmobile.union.lib.UnionWithClassInfo;
import com.besmartmobile.union.lib.annotations.UnionAnnotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@UnionAnnotation
public abstract class ValidationError extends UnionWithClassInfo {

    public <T> T match(@NonNull Function<EmptyError, T> emptyErrorFunction,
                       @NonNull Function<ServerError, T> serverErrorFunction) {
        if (this instanceof EmptyError) {
            return emptyErrorFunction.apply((EmptyError) this);
        }
        if (this instanceof ServerError) {
            return serverErrorFunction.apply((ServerError) this);
        }
        throw new IllegalStateException();
    }

    public static ValidationError emptyError() {
        return new EmptyError();
    }

    public static ValidationError serverError(@NonNull String message) {
        return new ServerError(message);
    }

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EmptyError extends ValidationError {
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ServerError extends ValidationError {
        @Getter @NonNull private String message;
    }
}
