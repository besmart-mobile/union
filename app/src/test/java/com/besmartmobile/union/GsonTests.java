package com.besmartmobile.union;


import com.besmartmobile.union.app.$UnionInfo;
import com.besmartmobile.union.app.gson.TestUnion;
import com.besmartmobile.union.gson.UnionTypeGsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.ArrayList;

import static com.besmartmobile.union.app.gson.TestUnionExt.variant1;
import static com.besmartmobile.union.app.gson.TestUnionExt.variant2;
import static com.besmartmobile.union.app.gson.TestUnionExt.variant3;
import static org.junit.Assert.assertEquals;

public class GsonTests {

    @Test
    public void correctly_serialize_and_deserialize() throws Exception {
        GsonBuilder gsonBuilder = new GsonBuilder();
        UnionTypeGsonUtils.registerAdapters(gsonBuilder, $UnionInfo.getunionClassWithClassInfoClasses());
        Gson gson = gsonBuilder.create();


        TestDto testDto = new TestDto();

        String json = gson.toJson(testDto);

        TestDto fromJson = gson.fromJson(json, TestDto.class);

        assertEquals("v2",
                ((TestUnion.Variant2 )fromJson.list.get(1)).getMessage());
    }

    static class TestDto {
        TestUnion v1 = variant1();
        TestUnion v2 = variant2("v2", 2);
        TestUnion v3 = variant3("v3", 3);
        ArrayList<TestUnion> list = new ArrayList<>();

        public TestDto() {
            list.add(v1);
            list.add(v2);
            list.add(v3);
        }
    }
}
