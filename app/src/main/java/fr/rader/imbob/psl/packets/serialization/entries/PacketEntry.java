package fr.rader.imbob.psl.packets.serialization.entries;

public class PacketEntry {

    private final String name;

    protected PacketEntry(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public final <T extends PacketEntry> T getAs(Class<T> clazz) {
        return clazz.cast(this);
    }
}
