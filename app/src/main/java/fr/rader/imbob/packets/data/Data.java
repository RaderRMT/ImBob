package fr.rader.imbob.packets.data;

public class Data {

    private final String name;
    private Object value;

    public Data(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
