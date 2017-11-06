package com.besmartmobile.union.gson;

import com.google.gson.GsonBuilder;

import java.util.List;

public class UnionTypeGsonUtils {

    private static UnionTypeAdapter unionTypeAdapter = new UnionTypeAdapter();

    public static void registerAdapters(GsonBuilder gsonBuilder,
                                        List<Class> unionClasses) {
        for (Class unionClass : unionClasses) {
            gsonBuilder.registerTypeAdapter(unionClass, unionTypeAdapter);
        }
    }
}
