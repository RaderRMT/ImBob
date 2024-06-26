package fr.rader.imbob.types.nbt;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.replay.ReplayMetaData;
import fr.rader.imbob.utils.data.DataReader;
import fr.rader.imbob.utils.data.DataWriter;

import java.util.ArrayList;
import java.util.List;

public class TagCompound extends TagBase {

    public static final byte TAG_ID = 10;

    private final List<TagBase> tags;

    private final boolean isRootTag;

    public TagCompound() {
        setID(TAG_ID);

        this.isRootTag = false;

        this.tags = new ArrayList<>();
    }

    public TagCompound(String name) {
        setID(TAG_ID);
        setName(name);

        this.isRootTag = false;

        this.tags = new ArrayList<>();
    }

    public TagCompound(String name, DataReader reader) {
        this(name, reader, false);
    }

    public TagCompound(String name, DataReader reader, boolean isRootTag) {
        setID(TAG_ID);
        setName(name);

        this.tags = new ArrayList<>();
        this.isRootTag = isRootTag;

        readCompound(reader);
    }

    private void readCompound(DataReader reader) {
        while (true) {
            int tagID = reader.readByte();
            if (tagID == 0) {
                return;
            }

            String tagName = reader.readString(reader.readShort());

            switch(tagID) {
                case 1:
                    add(new TagByte(tagName, reader.readByte()));
                    break;
                case 2:
                    add(new TagShort(tagName, reader.readShort()));
                    break;
                case 3:
                    add(new TagInt(tagName, reader.readInt()));
                    break;
                case 4:
                    add(new TagLong(tagName, reader.readLong()));
                    break;
                case 5:
                    add(new TagFloat(tagName, reader.readFloat()));
                    break;
                case 6:
                    add(new TagDouble(tagName, reader.readDouble()));
                    break;
                case 7:
                    add(new TagByteArray(tagName, reader.readFollowingBytes(reader.readInt())));
                    break;
                case 8:
                    add(new TagString(tagName, reader.readString(reader.readShort())));
                    break;
                case 9:
                    add(new TagList<>(tagName, reader));
                    break;
                case 10:
                    add(new TagCompound(tagName, reader));
                    break;
                case 11:
                    add(new TagIntArray(tagName, reader.readIntArray(reader.readInt())));
                    break;
                case 12:
                    add(new TagLongArray(tagName, reader.readLongArray(reader.readInt())));
                    break;
                default:
                    throw new IllegalStateException("Unexpected tag: " + Integer.toHexString(tagID));
            }
        }
    }

    @Override
    public void write(DataWriter writer) {
        if (getName() != null) {
            writer.writeByte(TAG_ID);
            if (!this.isRootTag) {
                writer.writeShort(getName().length());
                writer.writeString(getName());
            }
        }

        for (TagBase tag : tags) {
            tag.write(writer);
        }

        writer.writeByte(0);
    }

    public void add(TagBase nbt) {
        if (nbt.getName() == null) {
            throw new IllegalArgumentException("NBT Tag must have a name");
        }

        tags.add(nbt);
    }

    public void remove(String name) {
        this.tags.removeIf(tag -> tag.getName().equals(name));
    }

    public void remove(int index) {
        validateIndex("remove(index)", index);

        tags.remove(index);
    }

    public void remove(TagBase nbt) {
        tags.remove(nbt);
    }

    public void replace(int index, TagBase nbt) {
        validateIndex("replace(index, nbt)", index);

        if (nbt.getName() == null) {
            throw new IllegalArgumentException("NBT Tag must have a name");
        }

        tags.set(index, nbt);
    }

    public TagBase get(int index) {
        validateIndex("get(index)", index);

        return tags.get(index);
    }

    public TagBase get(String name) {
        for (TagBase tag : tags) {
            if (tag.getName().equals(name)) {
                return tag;
            }
        }

        return null;
    }

    public boolean has(String name) {
        return get(name) != null;
    }

    /**
     * Look if the list has something at the given index
     * @return true if the list contains something at the given index, false otherwise
     */
    public boolean has(int index) {
        return get(index) != null;
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public int size() {
        return tags.size();
    }

    public List<TagBase> getTags() {
        return tags;
    }

    private void validateIndex(String method, int index) {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("[TagCompound] -> [#" + method + "] cannot replace value in an empty list (index is " + index + ")");
        }

        if (index < 0 || index >= this.tags.size()) {
            throw new IndexOutOfBoundsException("[TagCompound] -> [#" + method + "] index must be " + ((this.tags.size() == 1) ? "" : "between 0 and ") + (this.tags.size() - 1) + " (index is " + index + ")");
        }
    }
}
