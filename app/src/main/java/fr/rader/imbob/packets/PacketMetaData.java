package fr.rader.imbob.packets;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.protocol.ProtocolVersion;

public class PacketMetaData {

    private final String name;

    private final Map<String, Integer> versions;

    public PacketMetaData() {
        this.name = "Missing Name";
        this.versions = new LinkedHashMap<>();
    }

    /**
     * Accepts a packet if the packet id matches
     *
     * @param packet    The packet to accept
     * @return          True if the packet is accepted, false otherwise
     */
    boolean accept(final Packet packet) {
        Protocol packetVersion = packet.getProtocol();

        // we loop through each version in the versions map
        for (Map.Entry<String, Integer> entry : this.versions.entrySet()) {
            if (packetVersion.isBeforeInclusive(ProtocolVersion.getInstance().get(entry.getKey()))) {
                // once we found the correct protocol, we check if
                // the packet id associated with it is the same
                // as the given packet id
                return entry.getValue() == packet.getPacketId().get();
            }
        }

        return false;
    }

    /**
     * Returns the packet id associated with a {@link ProtocolVersion}
     *
     * @param protocol  The {@link Protocol} to get the packet id from
     * @return          The associated packet id
     */
    public int getPacketIdForProtocol(final Protocol protocol) {
        // we loop through each version in the versions map
        for (Map.Entry<String, Integer> entry : this.versions.entrySet()) {
            // and we return the packet id once we found the correct protocol
            if (protocol.isBeforeInclusive(ProtocolVersion.getInstance().get(entry.getKey()))) {
                return entry.getValue();
            }
        }

        // if the packet id isn't defined, we return -1
        return -1;
    }

    public int getConfigurationPacketId(final Protocol protocol) {
        // we loop through each version in the versions map
        for (Map.Entry<String, Integer> entry : this.versions.entrySet()) {
            // and we return the packet id once we found the correct protocol
            if (ProtocolVersion.getInstance().get(entry.getKey()).isBeforeInclusive(protocol)) {
                return entry.getValue();
            }
        }

        // if the packet id isn't defined, we return -1
        return -1;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Integer> getVersions() {
        return this.versions;
    }
}
