package fr.rader.imbob.packets.data;

import java.util.ArrayList;
import java.util.List;

public class DataBlock {

    private final List<Data> data;

    public DataBlock() {
        this.data = new ArrayList<>();
    }

    public void add(final String name, final Object value) {
        add(new Data(name, value));
    }

    public void add(final Data data) {
        this.data.add(data);
    }

    public <T> T get(final String name, final Class<T> clazz) {
        Data data = get(name);
        if (data == null) {
            return null;
        }

        return clazz.cast(data.getValue());
    }

    public Data get(final String name) {
        return this.data.stream()
                    .filter(data -> data.getName().equals(name))
                    .findFirst()
                    .orElse(null);
    }

    public void update(final String name, final Object value) {
        if (!contains(name)) {
            return;
        }

        get(name).setValue(value);
    }

    public boolean contains(final String name) {
        return this.data.stream().anyMatch(data -> data.getName().equals(name));
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void putAll(final DataBlock data) {
        data.data.forEach(this::add);
    }
}
