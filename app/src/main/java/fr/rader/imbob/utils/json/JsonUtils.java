package fr.rader.imbob.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.utils.json.adapters.ProtocolAdapter;

import java.io.*;

public class JsonUtils {

    private static final Gson GSON;

    public static <T> T fromString(final String string, Class<T> clazz) {
        return GSON.fromJson(string, clazz);
    }

    public static <T> T fromStream(final InputStream inputStream, final Class<T> clazz) {
        return GSON.fromJson(new InputStreamReader(inputStream), clazz);
    }

    public static <T> T fromFile(final String path, Class<T> clazz) {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            return fromStream(inputStream, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    static {
        GSON = new GsonBuilder()
                .registerTypeAdapter(Protocol.class, new ProtocolAdapter())
                .create();
    }
}
