package fr.rader.imbob.psl.packets.serialization.entries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleArrayEntry extends PacketEntry implements Iterable<VariableEntry> {

    private final List<VariableEntry> variables;

    public SimpleArrayEntry(String name) {
        super(name);

        this.variables = new ArrayList<>();
    }

    public List<VariableEntry> getVariables() {
        return this.variables;
    }

    public void addVariable(VariableEntry value) {
        this.variables.add(value);
    }

    public void setVariable(int index, Object value) {
        VariableEntry entry = new VariableEntry(null);
        entry.setValue(value);

        this.variables.add(index, entry);
    }

    public VariableEntry getVariable(int index) {
        return this.variables.get(index);
    }

    @Override
    public Iterator<VariableEntry> iterator() {
        return this.variables.iterator();
    }

    @Override
    public String toString() {
        return "SimpleArrayEntry{" +
                "name=" + getName() +
                ", variables=" + this.variables +
                '}';
    }
}
