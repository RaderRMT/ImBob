package fr.rader.imbob.psl.packets.definition;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.psl.packets.definition.rules.Rule;
import fr.rader.imbob.types.VarInt;

import java.util.List;

public class PacketDefinition {

    /** The list of rules that describes
     * the format of our packet */
    private final List<Rule> rules;

    /** The name of the packet */
    private final String packetName;
    /** The ID of the packet
     * this PacketDefinition defines */
    private final VarInt packetID;

    private final Protocol protocolVersion;

    PacketDefinition(List<Rule> rules, VarInt packetID, Protocol protocolVersion) {
        // the first rule is always a Variable Rule,
        // with a type of PACKET, and it's name is the packet name,
        // so we get the name from the first rule
        this.packetName = rules.get(0).getName();
        // and we then remove it from the rules
        rules.remove(0);

        // we then keep the rules and the packet id
        this.rules = rules;
        this.packetID = packetID;
        this.protocolVersion = protocolVersion;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public String getPacketName() {
        return packetName;
    }

    public VarInt getPacketID() {
        return packetID;
    }

    public Protocol getProtocol() {
        return protocolVersion;
    }
}
