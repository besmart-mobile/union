package com.besmartmobile.union;

import com.besmartmobile.union.app.SomeUnion;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import static com.besmartmobile.union.app.SomeUnionExt.*;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        GsonBuilder gsonBuilder = new GsonBuilder();




        assertEquals(4, 2 + 2);
    }

    @Test
    public void union_ext_client_test() throws Exception {

        SomeUnion variant1 = variant1();
        String s = match(variant1,
                v1 -> "v1",
                v2 -> "v2");


        assertEquals("v1", s);
    }
}