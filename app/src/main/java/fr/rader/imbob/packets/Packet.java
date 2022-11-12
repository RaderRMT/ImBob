package fr.rader.imbob.packets;

import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.psl.packets.serialization.entries.PacketEntry;
import fr.rader.imbob.psl.packets.serialization.utils.EntryList;
import fr.rader.imbob.types.VarInt;

public class Packet {

    private final ProtocolVersion protocolVersion;
    private final VarInt packetId;

    private EntryList entries;
    private String packetName;

    public Packet(ProtocolVersion protocolVersion, VarInt packetId) {
        this.protocolVersion = protocolVersion;
        this.packetId = packetId;

        this.entries = new EntryList();
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protocolVersion;
    }

    public VarInt getPacketId() {
        return this.packetId;
    }

    public String getPacketName() {
        return this.packetName;
    }

    public void setPacketName(String packetName) {
        this.packetName = packetName;
    }

    /**
     * Add an entry to our packet, this will be added to the end of the list
     *
     * @param entry the entry to add
     */
    public void addEntry(PacketEntry entry) {
        this.entries.add(entry);
    }

    /**
     * Replace the entire list of packet entries with the one given as a parameter
     *
     * @param entry the new entries
     */
    public void setEntry(EntryList entry) {
        this.entries = entry;
    }

    public PacketEntry getEntry(String entryName) {
        for (PacketEntry entry : this.entries) {
            if (entry.getName().equals(entryName)) {
                return entry;
            }
        }

        return null;
    }

    public EntryList getEntries() {
        return this.entries;
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }
    
    /**
     * Clones this {@link Packet}.
     * It does not clone the entries contained in the packet.
     *
     * @return  The cloned packet
     */
    public Packet cloneEmpty() {
        Packet clonedPacket = new Packet(this.protocolVersion, this.packetId);
        clonedPacket.setPacketName(this.packetName);

        return clonedPacket;
    }

    /**
     * Clones this {@link Packet}.
     *
     * @return  The cloned packet
     */
    public Packet clone() {
        Packet clonedPacket = cloneEmpty();
        clonedPacket.setEntry(this.getEntries());
        clonedPacket.setPacketName(this.packetName);

        return clonedPacket;
    }
}
