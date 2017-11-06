package com.besmartmobile.union.app;


import com.besmartmobile.union.lib.UnionWithClassInfo;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

//public class UnionTypeAdapter implements JsonDeserializer<UnionWithClassInfo>, JsonSerializer<UnionWithClassInfo> {
//
//    @Override
//    public UnionWithClassInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
//            throws JsonParseException {
//        String actualClassName = json.getAsJsonObject().get("_actualClassName").getAsString();
//        try {
//            Class<?> actualClass = Class.forName(actualClassName);
//            final Object value = context.deserialize(json, actualClass);
//            return (UnionWithClassInfo) value;
//        } catch (ClassNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    @Override
//    public JsonElement serialize(UnionWithClassInfo src, Type typeOfSrc, JsonSerializationContext context) {
//        try {
//            Class<?> actualClass = Class.forName(src.getActualClassName());
//            return context.serialize(src, actualClass);
//        } catch (ClassNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//}