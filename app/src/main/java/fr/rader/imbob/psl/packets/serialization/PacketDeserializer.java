package fr.rader.imbob.psl.packets.serialization;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.psl.packets.definition.PacketDefinition;
import fr.rader.imbob.psl.packets.definition.rules.*;
import fr.rader.imbob.psl.packets.serialization.entries.ArrayEntry;
import fr.rader.imbob.psl.packets.serialization.entries.SimpleArrayEntry;
import fr.rader.imbob.psl.packets.serialization.entries.VariableEntry;
import fr.rader.imbob.psl.packets.serialization.utils.EntryList;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.utils.data.DataReader;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class PacketDeserializer {

    private final Stack<EntryList> variablesStack;

    private Protocol protocolVersion;
    private DataReader reader;

    public PacketDeserializer() {
        this.variablesStack = new Stack<>();
    }

    public void deserialize(PacketDefinition definition, Packet packet) throws IOException {
        this.protocolVersion = definition.getProtocol();

        packet.setEntry(deserializeCodeBlockFromRules(
                definition.getRules()
        ));

        packet.setPacketName(definition.getPacketName());
    }

    private EntryList deserializeCodeBlockFromRules(List<Rule> rules) throws IOException {
        // the entries in the current code block
        EntryList entries = new EntryList();
        // we push a new hashmap to the stack
        this.variablesStack.push(new EntryList());

        for (Rule rule : rules) {
            if (rule instanceof VariableRule) {
                deserializeVariable(
                        rule.getAs(VariableRule.class),
                        entries
                );
            }

            if (rule instanceof MatchRule) {
                deserializeMatch(
                        rule.getAs(MatchRule.class),
                        entries
                );
            }

            if (rule instanceof ArrayRule) {
                deserializeArray(
                        rule.getAs(ArrayRule.class),
                        entries
                );
            }

            if (rule instanceof SimpleArrayRule) {
                deserializeSimpleArray(
                        rule.getAs(SimpleArrayRule.class),
                        entries
                );
            }

            if (rule instanceof ConditionRule) {
                deserializeCondition(
                        rule.getAs(ConditionRule.class),
                        entries
                );
            }
        }

        // we pop the stack
        this.variablesStack.pop();
        // and we return the entries list
        // because we're done deserializing the current block
        return entries;
    }

    private void deserializeVariable(VariableRule rule, EntryList entries) throws IOException {
        VariableEntry entry = new VariableEntry(rule.getName());
        entry.setValue(this.reader.readFromTokenType(
                rule.getType(),
                this.protocolVersion
        ));

        entries.add(entry);

        this.variablesStack.peek().add(entry);
    }

    private void deserializeMatch(MatchRule rule, EntryList entries) throws IOException {
        int value = getValueFromVariable(rule.getVariable());

        List<Rule> matchBlockRules = rule.getRulesForValue(value);

        deserializeCodeBlockFromRules(matchBlockRules).forEach(entry -> entries.add(entry));
    }

    private void deserializeArray(ArrayRule rule, EntryList entries) throws IOException {
        int value = getValueFromVariable(rule.getLengthVariable());

        ArrayEntry entry = new ArrayEntry(rule.getName());
        for (int i = 0; i < value; i++) {
            entry.set(
                    i,
                    deserializeCodeBlockFromRules(
                            rule.getRules()
                    )
            );
        }

        entries.add(entry);
    }

    private void deserializeSimpleArray(SimpleArrayRule rule, EntryList entries) throws IOException {
        int value = getValueFromVariable(rule.getLengthVariable());

        SimpleArrayEntry entry = new SimpleArrayEntry(rule.getName());
        for (int i = 0; i < value; i++) {
            entry.setVariable(
                    i,
                    this.reader.readFromTokenType(
                            rule.getType(),
                            this.protocolVersion
                    )
            );
        }

        entries.add(entry);
    }

    private void deserializeCondition(ConditionRule rule, EntryList entries) throws IOException {
        int value = getValueFromVariable(rule.getVariable());

        if (!rule.isBranchTaken(value)) {
            return;
        }

        deserializeCodeBlockFromRules(rule.getBranchRules()).forEach(entry -> entries.add(entry));
    }

    /**
     * Look in the entire stack for the variable and return its value
     *
     * @param variableName  The variable to get the value from
     * @param type          The class to cast the variable's value to
     * @return              The variable's value
     */
    private <T> T getVariableValue(String variableName, Class<T> type) {
        for (EntryList list : this.variablesStack) {
            if (list.get(variableName) != null) {
                return list.get(variableName).getAs(VariableEntry.class).getValueAs(type);
            }
        }

        return null;
    }

    private int getValueFromVariable(String variableName) {
        // getting the variable from the variables stack
        Object variableValue = getVariableValue(variableName, Object.class);
        // if the variable doesn't exist, we throw an exception.
        // this should never happen, but it's still nice to have nonetheless
        if (variableValue == null) {
            throw new IllegalStateException("Undefined variable: " + variableName);
        }

        // this is the variable's value as an int.
        int value;
        if (variableValue instanceof VarInt) {
            // we get the value if it's from a VarInt
            value = ((VarInt) variableValue).getValue();
        } else {
            // we get the value from the object
            value = Integer.parseInt(variableValue.toString());
        }

        return value;
    }

    public void setDataReader(DataReader reader) {
        this.reader = reader;
    }
}
