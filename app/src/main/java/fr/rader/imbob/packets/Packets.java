package fr.rader.imbob.packets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.utils.OS;
import fr.rader.imbob.utils.json.JsonUtils;

public class Packets {

    private static Packets instance;

    private final List<PacketMetaData> packetData;

    private Packets() {
        this.packetData = new ArrayList<>();

        this.packetData.add(new PacketMetaData());
    }

    public static String getPSLWritePath(Protocol protocol, int packetId) {
        return getPath(protocol, packetId, "write");
    }

    public static String getPSLReadPath(Protocol protocol, int packetId) {
        return getPath(protocol, packetId, "read");
    }

    private static String getPath(final Protocol protocol, final int packetId, final String pslType) {
        StringBuilder path = new StringBuilder(OS.getAssetsFolder() + "protocols/");

        PacketMetaData packet = get(protocol, packetId);
        path.append(packet.getName());
        path.append('/');
        path.append(pslType);
        path.append('/');

        List<String> protocols = Arrays.asList(new File(path.toString()).list());
        Collections.sort(protocols);

        protocols.stream()
                .map(string -> Integer.parseInt(string.replace(".psl", "")))
                .filter(protocolId -> protocol.getVersion() >= protocolId)
                .reduce((first, second) -> second)
                .ifPresent(protocolId -> {
                    path.append(protocolId);
                    path.append(".psl");
                });

        return path.toString();
    }

    public static Packets getInstance() {
        if (instance == null) {
            instance = JsonUtils.fromFile(OS.getAssetsFolder() + "packets.json", Packets.class);
        }

        return instance;
    }

    public static PacketMetaData get(Protocol protocol, int packetId) {
        for (PacketMetaData data : getInstance().packetData) {
            if (data.getPacketIdForProtocol(protocol) == packetId) {
                return data;
            }
        }

        return null;
    }

    public static PacketMetaData get(String name) {
        for (PacketMetaData data : getInstance().packetData) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;
    }
}
