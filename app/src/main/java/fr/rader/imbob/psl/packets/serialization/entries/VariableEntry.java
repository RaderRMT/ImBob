package fr.rader.imbob.psl.packets.serialization.entries;

public class VariableEntry extends PacketEntry {

    private Object value;

    public VariableEntry(String name) {
        super(name);
    }

    public VariableEntry(String name, Object value) {
        super(name);

        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public <T> T getValueAs(Class<T> clazz) {
        return clazz.cast(this.value);
    }

    @Override
    public String toString() {
        return "VariableEntry{" +
                "name=" + getName() +
                ", value=" + this.value +
                '}';
    }
}
