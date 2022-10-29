package fr.rader.imbob.utils;

public class MathUtils {

    public static <T extends Comparable<T>> T clamp(T value, T min, T max) {
        if (value.compareTo(max) > 0) {
            return max;
        }

        if (value.compareTo(min) < 0) {
            return min;
        }

        return value;
    }
}
