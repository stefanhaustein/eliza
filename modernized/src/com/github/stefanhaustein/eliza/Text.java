package com.github.stefanhaustein.eliza;

import java.util.Arrays;

/**
 *  Eliza string functions.
 */
public class Text {


    static boolean match(String s, String pattern, String[] result) {
        int consumed = 0;
        int patternPos = 0;
        int count = 0;
        while (patternPos < pattern.length() && count < result.length) {
            if (pattern.charAt(patternPos) == '#') {
                patternPos++;
                int end = consumed;
                while (end < s.length() && Character.isDigit(s.charAt(end))) {
                    end++;
                }
                result[count++] = s.substring(consumed, end);
                consumed = end;
            } else {
                boolean star = pattern.charAt(patternPos) == '*';
                if (star) {
                    patternPos++;
                    if (patternPos == pattern.length()) {
                        result[count++] = s.substring(consumed);
                        break;
                    }
                }
                int end = patternPos;
                while (end < pattern.length()
                        && pattern.charAt(end) != '*' && pattern.charAt(end) != '#') {
                    end++;
                }
                if (end == patternPos) {
                    throw new IllegalStateException(pattern);
                }
                String search = pattern.substring(patternPos, end);
                patternPos = end;
                if (!star) {
                    if (!s.substring(consumed).startsWith(search)) {
                        return false;
                    }
                    consumed += search.length();
                } else {
                    int i = s.indexOf(search, consumed);
                    if (i == -1) {
                        return false;
                    }
                    result[count++] = s.substring(consumed, i);
                    consumed = i + search.length();
                }
            }
        }

    //    System.out.println("Split '" + s + "' into " + Arrays.toString(Arrays.copyOf(result, count)) + " using " +pattern);

        return true;
    }


    /**
     *  Translates corresponding characters in src to dest.
     */
    public static String replace(String str, String src, char dest) {
        for (int i = 0; i < src.length(); i++) {
            str = str.replace(src.charAt(i), dest);
        }
        return str;
    }

    /**
     *  Compresses its input by:
     *    dropping space before space, comma, and period;
     *    adding space before question, if char before is not a space; and
     *    copying all others
     */
    public static String compress(String s) {
        if (s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        char c = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (c == ' ' &&
                 ((s.charAt(i) == ' ') ||
                 (s.charAt(i) == ',') ||
                 (s.charAt(i) == '.'))) {
                    // nothing
            } else if (c != ' ' && s.charAt(i) == '?') {
                sb.append(c).append(' ');
            } else {
                sb.append(c);
            }
            c = s.charAt(i);
        }
        sb.append(c);
        return sb.toString();
    }


    /**
     *  Pad by ensuring there are spaces before and after the sentence.
     */
    public static String pad(String s) {
        if (!s.startsWith(" ")) {
            s = " " + s;
        }
        if (!s.endsWith(" ")){
            s = s + ' ';
        }
        return s;
    }

    /**
     *  Count number of occurrances of c in str
     */
    public static int count(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == c) count++;
        return count;
    }
}