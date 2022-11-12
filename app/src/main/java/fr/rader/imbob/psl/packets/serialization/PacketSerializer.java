package fr.rader.imbob.psl.packets.serialization;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.psl.packets.definition.PacketDefinition;
import fr.rader.imbob.psl.packets.definition.rules.*;
import fr.rader.imbob.psl.packets.serialization.entries.ArrayEntry;
import fr.rader.imbob.psl.packets.serialization.entries.PacketEntry;
import fr.rader.imbob.psl.packets.serialization.entries.SimpleArrayEntry;
import fr.rader.imbob.psl.packets.serialization.entries.VariableEntry;
import fr.rader.imbob.psl.packets.serialization.utils.EntryList;
import fr.rader.imbob.psl.tokens.TokenType;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.utils.data.DataWriter;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class PacketSerializer {

    private final Stack<EntryList> scope;

    private final DataWriter writer;

    private ProtocolVersion protocolVersion;

    private int offset;

    public PacketSerializer() throws IOException {
        this.writer = new DataWriter(false);
        this.scope = new Stack<>();
    }

    public void serialize(PacketDefinition definition, Packet packet) {
        this.writer.getData().clear();
        this.protocolVersion = packet.getProtocolVersion();
        this.offset = 0;

        serializeBlock(
                definition.getRules(),
                packet.getEntries()
        );
    }

    private void serializeBlock(List<Rule> rules, EntryList entries) {
        this.scope.push(entries);

        for (int i = 0; i < rules.size() && this.offset < entries.size(); i++) {
            Rule rule = rules.get(i);
            PacketEntry entry = entries.get(this.offset);

            if (entry == null) {
                throw new IllegalStateException("Missing entry for rule " + rule);
            }

            if (rule instanceof VariableRule) {
                serializeVariable(
                        rule.getAs(VariableRule.class),
                        entry.getAs(VariableEntry.class)
                );
            }

            if (rule instanceof MatchRule) {
                serializeMatch(
                        rule.getAs(MatchRule.class),
                        entries
                );
            }

            if (rule instanceof ArrayRule) {
                serializeArray(
                        rule.getAs(ArrayRule.class),
                        entry.getAs(ArrayEntry.class)
                );
            }

            if (rule instanceof SimpleArrayRule) {
                serializeSimpleArray(
                        rule.getAs(SimpleArrayRule.class),
                        entry.getAs(SimpleArrayEntry.class)
                );
            }

            if (rule instanceof ConditionRule) {
                serializeCondition(
                        rule.getAs(ConditionRule.class),
                        entries
                );
            }

            this.offset += 1;
        }

        this.scope.pop();
    }

    private void serializeVariable(VariableRule rule, VariableEntry entry) {
        serializeVariable(rule.getType(), entry);
    }

    private void serializeMatch(MatchRule rule, EntryList entries) {
        int value = getValueFromVariable(getVariableFromScope(rule.getVariable()));

        List<Rule> rules = rule.getRulesForValue(value);
        serializeBlock(rules, entries);

        this.offset--; // we remove the call to serializeMatch
    }

    private void serializeArray(ArrayRule rule, ArrayEntry entry) {
        int tempOffset = this.offset;
        this.offset = 0;

        for (EntryList entries : entry) {
            serializeBlock(rule.getRules(), entries);
            this.offset = 0;
        }

        this.offset = tempOffset; // we remove the call to serializeArray
    }

    private void serializeSimpleArray(SimpleArrayRule rule, SimpleArrayEntry entry) {
        for (VariableEntry variable : entry) {
            serializeVariable(
                    rule.getType(),
                    variable
            );
        }
    }

    private void serializeCondition(ConditionRule rule, EntryList entries) {
        int value = getValueFromVariable(entries.get(rule.getVariable()).getAs(VariableEntry.class));

        if (!rule.isBranchTaken(value)) {
            this.offset--; // we remove the call to serializeCondition
            return;
        }

        List<Rule> rules = rule.getBranchRules();
        serializeBlock(rules, entries);

        this.offset--; // we remove the call to serializeCondition
    }

    private void serializeVariable(TokenType type, VariableEntry entry) {
        this.writer.writeFromTokenType(
                type,
                entry.getValue(),
                this.protocolVersion
        );
    }

    private int getValueFromVariable(VariableEntry entry) {
        // getting the variable from the variables stack
        Object variableValue = entry.getValue();

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

    private VariableEntry getVariableFromScope(String variableName) {
        for (EntryList list : this.scope) {
            if (list.get(variableName) != null) {
                return list.get(variableName).getAs(VariableEntry.class);
            }
        }

        return null;
    }

    public List<Byte> getData() {
        return this.writer.getData();
    }
}
