package fr.rader.imbob.protocol;

import java.util.ArrayList;
import java.util.List;

import fr.rader.imbob.utils.OS;
import fr.rader.imbob.utils.json.JsonUtils;

public class ProtocolVersion {

    private static ProtocolVersion instance;

    private final List<Protocol> protocolVersions;

    private ProtocolVersion() {
        this.protocolVersions = new ArrayList<>();
    }

    public Protocol get(String protocolVersion) {
        for (Protocol protocol : this.protocolVersions) {
            if (protocol.getName().equals(protocolVersion)) {
                return protocol;
            }
        }

        return null;
    }

    public Protocol getFromId(int id) {
        for (Protocol protocol : this.protocolVersions) {
            if (protocol.getVersion() == id) {
                return protocol;
            }
        }

        return null;
    }

    public static ProtocolVersion getInstance() {
        if (instance == null) {
            instance = JsonUtils.fromFile(OS.getAssetsFolder() + "protocol_versions.json", ProtocolVersion.class);
        }

        return instance;
    }
}

