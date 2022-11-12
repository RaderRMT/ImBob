package fr.rader.imbob.psl.packets.serialization.utils;

import fr.rader.imbob.psl.packets.serialization.entries.PacketEntry;

import java.util.LinkedList;

public class EntryList extends LinkedList<PacketEntry> {

    public EntryList() {
        super();
    }

    /**
     * Get a {@link PacketEntry} in this {@link EntryList} by its name
     *
     * @param name      The name of the {@link PacketEntry} to find
     * @return          {@link PacketEntry}, or null if none were found
     */
    public PacketEntry get(String name) {
        for (PacketEntry entry : this) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }

        return null;
    }
}
