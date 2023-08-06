package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.packets.data.Data;
import fr.rader.imbob.packets.data.DataBlock;
import fr.rader.imbob.packets.data.DataBlockArray;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.annotations.Task;
import fr.rader.imbob.types.VarInt;
import imgui.ImGui;
import imgui.type.ImString;

@Task("Username Changer")
public class UsernameChangerTask extends AbstractTask {

    private static final int ACTION_ADD_PLAYER = 0;
    private static final int ACTION_ADD_PLAYER_BIT = 0x01;

    private final ImString targetUsername;
    private final ImString newUsername;

    public UsernameChangerTask() {
        this.targetUsername = new ImString();
        this.newUsername = new ImString();

        acceptPacket(PacketAcceptor.accept(Packets.get("player_info")));
    }

    private void edit761(final Packet packet) {
        int actionBitfield = packet.get("action", Integer.class);

        if ((actionBitfield & ACTION_ADD_PLAYER_BIT) == 0) {
            return;
        }

        DataBlockArray actions = packet.get("actions", DataBlockArray.class);
        for (DataBlock action : actions) {
            if (!action.contains("name")) {
                continue;
            }

            Data usernameData = action.get("name");

            if (usernameData.getValue().equals(this.targetUsername.get())) {
                usernameData.setValue(this.newUsername.get());
            }
        }
    }

    private void edit(final Packet packet) {
        int action = packet.get("action", VarInt.class).get();

        if (action != ACTION_ADD_PLAYER) {
            return;
        }

        DataBlockArray players = packet.get("players", DataBlockArray.class);
        for (DataBlock player : players) {
            Data usernameData = player.get("name");

            if (usernameData.getValue().equals(this.targetUsername.get())) {
                usernameData.setValue(this.newUsername.get());
            }
        }
    }

    @Override
    protected void execute(Packet packet, Queue<Packet> packets) {
        if (packet.getProtocol().isAfterInclusive(ProtocolVersion.getInstance().get("MC_1_19_3"))) {
            edit761(packet);
        } else {
            edit(packet);
        }
    }

    @Override
    public void render() {
        ImGui.inputText("Target Username", this.targetUsername);
        ImGui.inputText("New Username", this.newUsername);
        ImGui.text("Target username is the username of\nthe player you want to change");
    }
}
