package fr.rader.imbob.protocol;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import fr.rader.imbob.utils.OS;

public class ProtocolVersion {

    private static ProtocolVersion instance;

    private final List<Protocol> protocolVersions;

    private ProtocolVersion() {
        this.protocolVersions = new ArrayList<>();
    }

    public static ProtocolVersion getInstance() {
        if (instance == null) {
            try (FileReader reader = new FileReader(OS.getImBobFolder() + "protocol_versions.json")) {
                instance = new Gson().fromJson(reader, ProtocolVersion.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public static final Protocol get(String protocolVersion) {
        for (Protocol protocol : getInstance().protocolVersions) {
            if (protocol.getName().equals(protocolVersion)) {
                return protocol;
            }
        }

        return null;
    }

    public static final Protocol getFromId(int id) {
        for (Protocol protocol : getInstance().protocolVersions) {
            if (protocol.getVersion() == id) {
                return protocol;
            }
        }

        return null;
    }
}

