package fr.rader.imbob.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.rader.imbob.utils.json.JsonUtils;
import fr.rader.imbob.windows.impl.LoggerWindow;

public class MojangAPI {

    private static final String MOJANG_API_USER_PROFILE = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_API_SESSION_PROFILE = "https://sessionserver.mojang.com/session/minecraft/profile/";

    /**
     * Get a user's skin data from Mojang's servers
     *
     * @param name      The user's name to get the skin from
     * @return          null if we are being rate-rated or if the user doesn't exist.
     *                  Otherwise, a String[] will be returned:
     *                    - out[0] is the value
     *                    - out[1] is the signature
     */
    public static String[] getSkinData(String name) {
        String uuid = getUUID(name);
        if (uuid == null) {
            return null;
        }

        String[] out = new String[2];

        String data = getDataFromMojang(MOJANG_API_SESSION_PROFILE, uuid + "?unsigned=false"); // we add ?unsigned=false to the argument because we want to get the signature for the skin
        JsonObject root = JsonUtils.fromString(data, JsonObject.class);

        JsonArray properties = root.getAsJsonArray("properties");
        for (JsonElement element : properties) {
            JsonObject property = element.getAsJsonObject();

            if (!property.get("name").getAsString().equals("textures")) {
                continue;
            }

            out[0] = property.get("value").getAsString();
            out[1] = property.get("signature").getAsString();
            break;
        }

        return out;
    }

    /**
     * Get a user's UUID from Mojang's servers
     *
     * @param username  The username to get the UUID associated to it
     * @return          null if we are being rate-rated or if the user doesn't exist.
     *                  Otherwise, the user's UUID will be returned as a String
     */
    private static String getUUID(String username) {
        String uuid = getDataFromMojang(
                MOJANG_API_USER_PROFILE,
                username
        );

        if (uuid == null) {
            return null;
        }

        JsonObject object = JsonUtils.fromString(uuid, JsonObject.class);
        return object.get("id").getAsString();
    }

    /**
     * Get some data from Mojang's servers
     *
     * @param url       The URL to fetch data from
     * @param argument  The argument to append to the URL (either the UUID or username)
     * @return          null if we are being rate-rated or if the user doesn't exist.
     *                  Otherwise, the data will be returned as a json String
     */
    private static String getDataFromMojang(String url, String argument) {
        StringBuilder receivedJson = new StringBuilder();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url + argument).openConnection();
            if (connection.getResponseCode() == 429) {
                LoggerWindow.error("You are being rate-limited!");
                return null;
            }

            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String input;
            while ((input = reader.readLine()) != null) {
                receivedJson.append(input);
            }

            reader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (receivedJson.toString().isEmpty()) {
            return null;
        }

        return receivedJson.toString();
    }
}
