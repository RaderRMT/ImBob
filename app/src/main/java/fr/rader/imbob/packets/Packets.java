package fr.rader.imbob.packets;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.utils.OS;

public class Packets {

    private static Packets instance;

    private final List<PacketData> packetData;

    private Packets() {
        this.packetData = new ArrayList<>();

        this.packetData.add(new PacketData());
    }

    public static String getPSLPath(Protocol protocol, int packetId) {
        StringBuilder path = new StringBuilder(OS.getImBobFolder() + "protocols/");

        PacketData packet = get(protocol, packetId);
        path.append(packet.getName());
        path.append('/');

        List<String> protocols = Arrays.asList(new File(path.toString()).list());
        Collections.sort(protocols);
        
        for (int i = protocols.size() - 1; i >= 0; i--) {
            int protocolId = Integer.parseInt(protocols.get(i).replace(".psl", ""));

            if (protocol.getVersion() >= protocolId) {
                path.append(protocolId);
                path.append(".psl");

                break;
            }
        }

        return path.toString();
    }

    public static Packets getInstance() {
        if (instance == null) {
            try (FileReader reader = new FileReader(OS.getImBobFolder() + "packets.json")) {
                instance = new Gson().fromJson(reader, Packets.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    public static PacketData get(Protocol protocol, int packetId) {
        for (PacketData data : getInstance().packetData) {
            if (data.getPacketIdForProtocol(protocol) == packetId) {
                return data;
            }
        }

        return null;
    }

    public static PacketData get(String name) {
        for (PacketData data : getInstance().packetData) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;
    }
}
