package fr.rader.imbob.psl.packets.serialization.entries;

import fr.rader.imbob.psl.packets.serialization.utils.EntryList;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArrayEntry extends PacketEntry implements Iterable<EntryList> {

    private final Map<Integer, EntryList> entries;

    public ArrayEntry(String name) {
        super(name);

        this.entries = new LinkedHashMap<>();
    }

    /**
     * Return an entry in the array.<br>
     * Let's consider this array:<br>
     * <br>
     * <pre>
     *     int "length";
     *     array("name", "length") {
     *         int "one";
     *         int "two";
     *         int "three";
     *     }
     * </pre>
     * Let's imagine the length variable is 3 and our data is this:<br>
     * <br>
     * <pre>
     *     0x01, 0x02, 0x03
     *     0x04, 0x05, 0x06
     *     0x07, 0x08, 0x09
     * </pre>
     * Our array will look like this:<br>
     * <br>
     * <pre>
     *     "name"
     *       |- 0
     *       |  |- "one"    (0x01)
     *       |  |- "two"    (0x02)
     *       |  \- "three"  (0x03)
     *       |- 1
     *       |  |- "one"    (0x04)
     *       |  |- "two"    (0x05)
     *       |  \- "three"  (0x06)
     *       \- 2
     *          |- "one"    (0x07)
     *          |- "two"    (0x08)
     *          \- "three"  (0x09)
     * </pre>
     * Giving an index of 1 will return the second entry,<br>
     * so the one where "one" equals 4, "two" equals 5 and "three" equals 6
     *
     * @param index the entry index
     * @return {@link EntryList} - the list of entries at the given array index
     */
    public EntryList get(int index) {
        return entries.get(index);
    }

    /**
     * Set a list of packet entries at a given array index
     *
     * @param index the entry index
     * @param entries {@link EntryList} - the list of entries to add
     */
    public void set(int index, EntryList entries) {
        this.entries.put(index, entries);
    }

    public void add(EntryList entries) {
        set(size(), entries);
    }

    public int size() {
        return this.entries.size();
    }

    @Override
    public Iterator<EntryList> iterator() {
        return this.entries.values().iterator();
    }

    @Override
    public String toString() {
        return "ArrayEntry{" +
                "name=" + getName() +
                ", size=" + this.entries.size() +
                ", entries=" + this.entries +
                '}';
    }
}
