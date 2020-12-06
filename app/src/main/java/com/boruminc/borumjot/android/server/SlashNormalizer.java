package com.boruminc.borumjot.android.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SlashNormalizer {
    /**
     * https://regex101.com/r/g9Islb/1
     * @param escapedStr
     * @return
     */
    public static String unescapeControlCharacters(String escapedStr) {
        //                    (?<!\\)  (\\)  ([nrt'"])
        final String regex = "(?<!\\\\)(\\\\)([nrt'\"])";
        final String subst = "\n";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(escapedStr);

        return matcher.replaceAll(subst);
    }

    /**
     * Unescape characters that user has put in that would normally be unescaped
     * but were ignored in {@link SlashNormalizer#unescapeControlCharacters(String)}
     * for this very purpose
     * @param userSlashEscapedStr
     * @return The string with the user slashes unescaped
     */
    public static String unescapeUserSlashes(String userSlashEscapedStr) {
        /*
            Meaning                    | Regex Fragment
            ===========================================
            1st Capturing Group         (    )
                                          \ \
            2nd Capturing Group         (        )
                                                [      ]
                                                 nrt' "
        */
        final String userSlashesRegex = "(\\\\)([nrt'\"])";

        final String userSubstr = "\\n";
        final Pattern pattern = Pattern.compile(userSlashesRegex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(userSlashEscapedStr);

        return matcher.replaceAll(userSubstr);
    }
}
