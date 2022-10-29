package fr.rader.imbob.psl.packets.serialization.entries;

public class PacketEntry {

    private final String name;

    public PacketEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public <T extends PacketEntry> T getAs(Class<T> clazz) {
        return clazz.cast(this);
    }
}
