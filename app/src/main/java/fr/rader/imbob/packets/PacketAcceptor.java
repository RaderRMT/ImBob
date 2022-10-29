package fr.rader.imbob.packets;

import fr.rader.imbob.protocol.ProtocolVersion;

public class PacketAcceptor {

    private final Packets packetToAccept;

    private ProtocolVersion fromVersion;
    private ProtocolVersion toVersion;

    private PacketAcceptor(Packets packet) {
        this.packetToAccept = packet;
    }

    /**
     * Create a new {@link PacketAcceptor} for the given {@link Packets}
     *
     * @param packet    The packet to accept
     * @return          A new {@link PacketAcceptor} for the {@link Packets}
     */
    public static PacketAcceptor accept(Packets packet) {
        return new PacketAcceptor(packet);
    }

    /**
     * Accept the packet starting from the given {@link ProtocolVersion} (included)
     *
     * @param fromVersion       The version to accept the packet from
     * @return                  The {@link PacketAcceptor} instance to chain methods
     */
    public PacketAcceptor from(ProtocolVersion fromVersion) {
        this.fromVersion = fromVersion;
        return this;
    }

    /**
     * Accept the packet up to the given {@link ProtocolVersion} (included)
     *
     * @param toVersion         The version to accept the packet up to
     * @return                  The {@link PacketAcceptor} instance to chain methods
     */
    public PacketAcceptor to(ProtocolVersion toVersion) {
        this.toVersion = toVersion;
        return this;
    }

    /**
     * Accepts a packet.<br>
     * A packet is accepted if its protocol version is between {@link PacketAcceptor#fromVersion} and {@link PacketAcceptor#toVersion}, and if the packet id is accepted by the {@link Packets}.<br>
     * If {@link PacketAcceptor#fromVersion} is null, any packet from protocol {@link ProtocolVersion#MC_1_8} up to {@link PacketAcceptor#toVersion} are accepted.<br>
     * If {@link PacketAcceptor#toVersion} is null, any packet from {@link PacketAcceptor#fromVersion} and later are accepted.<br>
     * If both {@link PacketAcceptor#fromVersion} and {@link PacketAcceptor#toVersion} are null, the packet will be accepted.<br>
     *
     * @param packet    The packet to accept
     * @return          True if the packet is accepted, false otherwise
     */
    public boolean accept(Packet packet) {
        if (this.fromVersion != null && this.toVersion != null) {
            return packet.getProtocolVersion().isAfterInclusive(this.fromVersion) &&
                   packet.getProtocolVersion().isBeforeInclusive(this.toVersion) &&
                   this.packetToAccept.accept(packet);
        }

        if (this.fromVersion == null && this.toVersion != null) {
            return packet.getProtocolVersion().isBeforeInclusive(this.toVersion) &&
                   this.packetToAccept.accept(packet);
        }

        if (this.fromVersion != null && this.toVersion == null) {
            return packet.getProtocolVersion().isAfterInclusive(this.fromVersion) &&
                   this.packetToAccept.accept(packet);
        }

        return this.packetToAccept.accept(packet);
    }
}
