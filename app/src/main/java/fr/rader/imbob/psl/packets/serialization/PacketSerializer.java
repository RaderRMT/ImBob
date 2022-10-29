package fr.rader.imbob.psl.packets.serialization;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.psl.packets.definition.PacketDefinition;
import fr.rader.imbob.psl.packets.definition.rules.*;
import fr.rader.imbob.psl.packets.serialization.entries.*;
import fr.rader.imbob.psl.tokens.TokenType;
import fr.rader.imbob.utils.data.DataWriter;

import java.io.IOException;
import java.util.List;

public class PacketSerializer {

    private final DataWriter writer;

    private ProtocolVersion protocolVersion;

    public PacketSerializer() throws IOException {
        this.writer = new DataWriter(false);
    }

    /**
     * Serialize a {@link Packet} object to a {@link DataWriter} based on the {@link PacketDefinition}
     *
     * @param definition the packet's definition.
     *                   It contains instructions on how to read and write a specific packet
     * @param packet the packet to write
     */
    public void serialize(PacketDefinition definition, Packet packet) {
        this.writer.getData().clear();
        this.protocolVersion = packet.getProtocolVersion();
        serializeBlock(definition.getRules(), packet.getEntries());
    }

    /**
     * Serialize a list of {@link PacketEntry} to a {@link DataWriter} depending on a list of {@link Rule}
     *
     * @param rules the rules to serialize the entries
     * @param entries the entries to serialize
     */
    private void serializeBlock(List<Rule> rules, List<PacketEntry> entries) {
        // we loop through each rules
        for (int i = 0; i < rules.size(); i++) {
            // we get both the rule and the entry
            Rule rule = rules.get(i);
            PacketEntry entry = entries.get(i);

            // then we check the type of the entry.
            // if the entry is a variable entry
            if (entry instanceof VariableEntry) {
                // we write the variable to the data writer
                serializeVariable(
                        (VariableRule) rule,
                        (VariableEntry) entry
                );
            }

            // if the entry is an array entry
            if (entry instanceof ArrayEntry) {
                // we get the entry as an ArrayEntry
                // and the list of rules that defines
                // how the array is defined
                ArrayEntry arrayEntry = (ArrayEntry) entry;
                List<Rule> arrayRules = ((ArrayRule) rule).getRules();

                // then we loop through each entry in our array entry
                for (int entryIndex = 0; entryIndex < arrayEntry.size(); entryIndex++) {
                    // and we recursively call serializeBlock to
                    // serialize the array
                    serializeBlock(
                            arrayRules,
                            arrayEntry.getEntriesForIndex(entryIndex)
                    );
                }
            }

            // if the entry is a "simple array" entry
            // (a.k.a. byte["length"] "name")
            if (entry instanceof SimpleArrayEntry) {
                SimpleArrayRule simpleArrayRule = (SimpleArrayRule) rule;
                SimpleArrayEntry simpleArrayEntry = (SimpleArrayEntry) entry;

                // we loop through each variable in the array
                for (VariableEntry variableEntry : simpleArrayEntry.getVariables()) {
                    // and we serialize it
                    serializeVariable(
                            simpleArrayRule.getType(),
                            variableEntry
                    );
                }
            }

            // if the entry is a condition entry
            if (entry instanceof ConditionEntry) {
                // we get the entry as a condition entry
                ConditionEntry conditionEntry = (ConditionEntry) entry;

                // and we look if the entries list isn't empty.
                // if it is, that means the condition wasn't met
                // and there's nothing to write.
                if (conditionEntry.getEntries().size() != 0) {
                    // if the entries list isn't empty,
                    // we serialize it
                    serializeBlock(
                            ((ConditionRule) rule).getBranchRules(),
                            conditionEntry.getEntries()
                    );
                }
            }

            // if the entry is a match entry
            if (entry instanceof MatchEntry) {
                // we get the entry as a match entry
                MatchEntry matchEntry = (MatchEntry) entry;

                // and we serialize its content
                serializeBlock(
                        ((MatchRule) rule).getRulesForValue(matchEntry.getValue()),
                        matchEntry.getEntries()
                );
            }
        }
    }

    /**
     * Serialize a {@link VariableEntry} based on the {@link VariableRule}
     *
     * @param rule the variable rule
     * @param entry the variable entry to serialize
     */
    private void serializeVariable(VariableRule rule, VariableEntry entry) {
        serializeVariable(rule.getType(), entry);
    }

    private void serializeVariable(TokenType type, VariableEntry entry) {
        writer.writeFromTokenType(
                type,
                entry.getValue(),
                this.protocolVersion
        );
    }

    public List<Byte> getData() {
        return this.writer.getData();
    }
}
