package org.lushplugins.followers.utils;

public class StringUtils {

    public static String makeFriendly(String string) {
        StringBuilder output = new StringBuilder();

        String[] words = string.toLowerCase().split(" ");
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                output.append(" ");
            }

            String word = words[i];
            output.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return output.toString();
    }
}
