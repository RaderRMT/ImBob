package fr.rader.imbob.psl.packets.serialization.entries;

public class VariableEntry extends PacketEntry {

    /**
     * The variable's value
     */
    private Object value;

    public VariableEntry(String name, Object value) {
        super(name);

        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValueAs(Class<T> clazz) {
        return clazz.cast(this);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableEntry{" +
                "name=" + getName() +
                ", value=" + value +
                '}';
    }
}
