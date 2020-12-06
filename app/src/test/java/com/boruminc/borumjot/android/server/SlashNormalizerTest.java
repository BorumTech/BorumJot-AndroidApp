package com.boruminc.borumjot.android.server;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlashNormalizerTest {
    private String ctrlCharExpectedStr;

    @Before
    public void setUp() {
        ctrlCharExpectedStr = "My first line is here\nMy second line is here\nMy third line is here";
    }

    @Test
    public void unescapeControlCharactersWithAlreadyUnescapedStr_isCorrect() {
        String testStr = "My first line is here\nMy second line is here\nMy third line is here";

        String actualUnescapedResult = SlashNormalizer.unescapeControlCharacters(testStr);

        assertEquals(ctrlCharExpectedStr, actualUnescapedResult);
    }

    @Test
    public void unescapeControlCharactersWithEscapedNewLine_isCorrect() {
        String testStr = StringEscapeUtils.escapeJava(ctrlCharExpectedStr);

        String actualUnescapedResult = SlashNormalizer.unescapeControlCharacters(testStr);

        assertEquals(ctrlCharExpectedStr, actualUnescapedResult);
    }

    @Test
    public void unescapeUserSlashes_isCorrect() {
        String testStr = "Hello\\\\nworld";
        String actualUnescapedResult = SlashNormalizer.unescapeUserSlashes(testStr);
        assertEquals("Hello\\nworld", actualUnescapedResult);
    }
}
