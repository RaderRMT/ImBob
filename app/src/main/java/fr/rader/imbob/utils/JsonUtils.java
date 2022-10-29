package fr.rader.imbob.utils;

import com.google.gson.JsonArray;

public class JsonUtils {

    public static String[] getAsStringArray(JsonArray array) {
        String[] values = new String[array.size()];

        for (int i = 0; i < array.size(); i++) {
            values[i] = array.get(i).getAsString();
        }

        return values;
    }
}
