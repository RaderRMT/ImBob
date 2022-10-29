package fr.rader.imbob.types.nbt;

import fr.rader.imbob.utils.data.DataWriter;

public class TagDouble extends TagBase {

    public static final byte TAG_ID = 6;

    private double value;

    public TagDouble(double value) {
        setID(TAG_ID);
        setValue(value);
    }

    public TagDouble(String name, double value) {
        setID(TAG_ID);
        setName(name);
        setValue(value);
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void write(DataWriter writer) {
        if (getName() != null) {
            writer.writeByte(TAG_ID);
            writer.writeShort(getName().length());
            writer.writeString(getName());
        }

        writer.writeDouble(value);
    }
}
