package fr.rader.imbob.utils;

public class StringUtils {

    public static int indexOf(String string, char character, int offset) {
        int i = 0;
        int position = 0;
        for (char c : string.toCharArray()) {
            if (i == offset) {
                return position;
            }

            if (c == character) {
                i++;
            }

            position++;
        }

        return position;
    }

    public static int leadingSpaces(String string) {
        int length = 0;

        if (string != null) {
            while (string.charAt(length) == ' ') {
                length++;
            }
        }

        return length;
    }

    public static int length(String string) {
        int length = 0;

        if (string != null) {
            for (char c : string.toCharArray()) {
                if (c < 0x80 || c > 0xbf) {
                    length++;
                }
            }
        }

        return length;
    }

    public static boolean hasUpperCaseLetters(String string) {
        for (char c : string.toCharArray()) {
            if (CharacterUtils.isAlphaUpper(c)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSnakeCase(String string) {
        for (char c : string.toCharArray()) {
            if (c != '_' &&
                    !CharacterUtils.isAlphaLower(c) &&
                    !CharacterUtils.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean containsIgnoreCase(String source, String toFind) {
        return source.toLowerCase().contains(toFind.toLowerCase());
    }
}
