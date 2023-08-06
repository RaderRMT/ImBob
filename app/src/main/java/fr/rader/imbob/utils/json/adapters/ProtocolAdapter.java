package fr.rader.imbob.utils.json.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import fr.rader.imbob.protocol.Protocol;

import java.io.IOException;

public class ProtocolAdapter extends TypeAdapter<Protocol> {

    @Override
    public void write(JsonWriter out, Protocol value) throws IOException {
        // we will never write protocol stuff
    }

    @Override
    public Protocol read(JsonReader in) throws IOException {
        String name = "Missing Name";
        int version = -1;

        in.beginObject();

        do {
            switch (in.nextName()) {
                case "name":    name = in.nextString(); break;
                case "version": version = in.nextInt(); break;

                default: break;
            }
        } while (in.peek() == JsonToken.NAME);

        in.endObject();

        return new Protocol(name, version);
    }
}
