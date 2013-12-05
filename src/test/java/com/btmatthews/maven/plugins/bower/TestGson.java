package com.btmatthews.maven.plugins.bower;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: bmatthews68
 * Date: 20/10/2013
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class TestGson {

    @Test
    public void test() throws Exception {
        final Gson gson = new Gson();
        final String[] result = gson.fromJson("'hello'", String[].class);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("hello", result[0]);
    }
}
