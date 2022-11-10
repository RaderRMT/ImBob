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
            pair(MC_1_8,    0x2B),
            pair(MC_1_14_4, 0x1E),
            pair(MC_1_15_2, 0x1F),
            pair(MC_1_16_1, 0x1E),
            pair(MC_1_16_5, 0x1D),
            pair(MC_1_18_2, 0x1E),
            pair(MC_1_19,   0x1B),
            pair(MC_1_19_2, 0x1D)
    ),

    PLAYER_INFO(
            pair(MC_1_8,    0x38),
            pair(MC_1_12,   0x2D),
            pair(MC_1_12_2, 0x2E),
            pair(MC_1_14_4, 0x33),
            pair(MC_1_15_2, 0x34),
            pair(MC_1_16_1, 0x33),
            pair(MC_1_16_5, 0x32),
            pair(MC_1_18_2, 0x36),
            pair(MC_1_19,   0x34),
            pair(MC_1_19_2, 0x37)
    );

    private final Map<ProtocolVersion, Integer> versions;

    @SafeVarargs
    private Packets(Pair<ProtocolVersion, Integer>... ids) {
        // versions is a LinkedHashMap because i
        // want to preserve the insertion order
        this.versions = new LinkedHashMap<>();

        // add all pairs to the versions map
        for (Pair<ProtocolVersion, Integer> pair : ids) {
            this.versions.put(pair.getKey(), pair.getValue());
        }
    }

    /**
     * Pair a packet id to a {@link ProtocolVersion}
     *
     * @param version   The version to pair the id with
     * @param id        The packet id to pair to the protocol version
     * @return          A single object containing both the {@link ProtocolVersion} and packet id
     */
    private static Pair<ProtocolVersion, Integer> pair(ProtocolVersion version, int id) {
        return new Pair<ProtocolVersion, Integer>(version, id);
    }

    /**
     * Accepts a packet if the packet id matches
     *
     * @param packet    The packet to accept
     * @return          True if the packet is accepted, false otherwise
     */
    boolean accept(Packet packet) {
        ProtocolVersion packetVersion = packet.getProtocolVersion();

        // we loop through each version in the versions map
        for (Map.Entry<ProtocolVersion, Integer> entry : this.versions.entrySet()) {
            if (packetVersion.isBeforeInclusive(entry.getKey())) {
                // once we found the correct protocol, we check if
                // the packet id associated with it is the same
                // as the given packet id
                return entry.getValue() == packet.getPacketId().getValue();
            }
        }

        return false;
    }

    /**
     * Returns the packet id associated with a {@link ProtocolVersion}
     *
     * @param protocolVersion   The {@link ProtocolVersion} to get the packet id from
     * @return                  The associated packet id
     */
    public int getPacketIdForProtocol(ProtocolVersion protocolVersion) {
        // we loop through each version in the versions map
        for (Map.Entry<ProtocolVersion, Integer> entry : this.versions.entrySet()) {
            // and we return the packet id once we found the correct protocol
            if (protocolVersion.isBeforeInclusive(entry.getKey())) {
                return entry.getValue();
            }
        }

        // if the packet id isn't defined, we return -1
        return -1;
    }
}
