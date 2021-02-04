package com.boruminc.borumjot.android.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlashNormalizerTest {
    @Test
    public void unescapeUserSlashes_isCorrect() {
        String testStr = "Hello\\nworld";
        String actualUnescapedResult = SlashNormalizer.unescapeUserSlashes(testStr);
        assertEquals("Hello\nworld", actualUnescapedResult);
    }
}
