package fr.rader.imbob.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.utils.Pair;

import static fr.rader.imbob.protocol.ProtocolVersion.*;

public enum Packets {

    TIME_UPDATE(
            pair(MC_1_8,    0x03),
            pair(MC_1_11_2, 0x44),
            pair(MC_1_12,   0x46),
            pair(MC_1_12_2, 0x47),
            pair(MC_1_14_4, 0x4E),
            pair(MC_1_15_2, 0x4F),
            pair(MC_1_16_5, 0x4E),
            pair(MC_1_17_1, 0x58),
            pair(MC_1_18_2, 0x59),
            pair(MC_1_19,   0x59),
            pair(MC_1_19_2, 0x5C)
    ),

    JOIN_GAME(
            pair(MC_1_8,    0x01),
            pair(MC_1_12_2, 0x23),
            pair(MC_1_14_4, 0x25),
            pair(MC_1_15_2, 0x26),
            pair(MC_1_16_1, 0x25),
            pair(MC_1_16_5, 0x24),
            pair(MC_1_18_2, 0x26),
            pair(MC_1_19,   0x23),
            pair(MC_1_19_2, 0x25)
    ),

    CHANGE_GAME_STATE(
            pair(MC_1_8, 0x2B),
            pair(MC_1_14_4, 0x1E),
            pair(MC_1_15_2, 0x1F),
            pair(MC_1_16_1, 0x1E),
            pair(MC_1_16_5, 0x1D),
            pair(MC_1_18_2, 0x1E),
            pair(MC_1_19, 0x1B),
            pair(MC_1_19_2, 0x1D)
    );

    private final Map<ProtocolVersion, Integer> versions;

    @SafeVarargs
    private Packets(Pair<ProtocolVersion, Integer>... ids) {
        this.versions = new LinkedHashMap<>();

        for (Pair<ProtocolVersion, Integer> pair : ids) {
            this.versions.put(pair.getKey(), pair.getValue());
        }
    }

    private static Pair<ProtocolVersion, Integer> pair(ProtocolVersion version, int id) {
        return new Pair<ProtocolVersion, Integer>(version, id);
    }

    boolean shouldAccept(Packet packet) {
        ProtocolVersion packetVersion = packet.getProtocolVersion();

        for (Map.Entry<ProtocolVersion, Integer> entry : this.versions.entrySet()) {
            if (packetVersion.isBeforeInclusive(entry.getKey())) {
                return entry.getValue() == packet.getPacketId().getValue();
            }
        }

        return false;
    }

    public int getPacketIdForProtocol(ProtocolVersion protocolVersion) {
        for (Map.Entry<ProtocolVersion, Integer> entry : this.versions.entrySet()) {
            if (protocolVersion.isBeforeInclusive(entry.getKey())) {
                return entry.getValue();
            }
        }

        return -1;
    }
}
