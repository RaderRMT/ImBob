package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.psl.packets.serialization.entries.ArrayEntry;
import fr.rader.imbob.psl.packets.serialization.entries.VariableEntry;
import fr.rader.imbob.psl.packets.serialization.utils.EntryList;
import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.annotations.Task;
import fr.rader.imbob.types.VarInt;
import imgui.ImGui;
import imgui.type.ImString;

@Task("Username Changer")
public class UsernameChangerTask extends AbstractTask {

    private static final int ACTION_ADD_PLAYER = 0;
    private static final int ACTION_UPDATE_DISPLAY_NAME = 3;

    private final ImString targetUsername;
    private final ImString newUsername;

    public UsernameChangerTask() {
        this.targetUsername = new ImString();
        this.newUsername = new ImString();

        acceptPacket(PacketAcceptor.accept(Packets.get("player_info")));
    }

    @Override
    protected void execute(Packet packet, Queue<Packet> packets) {
        int action = packet.getEntry("action")
                           .getAs(VariableEntry.class)
                           .getValueAs(VarInt.class)
                           .getValue();

        switch (action) {
            case ACTION_ADD_PLAYER:
                ArrayEntry players = packet.getEntry("players").getAs(ArrayEntry.class);

                for (EntryList player : players) {
                    VariableEntry username = player.get("name").getAs(VariableEntry.class);

                    if (username.getValueAs(String.class).equals(this.targetUsername.get())) {
                        username.setValue(this.newUsername.get());
                    }
                }

                break;

            // i am keeping this for later
            case ACTION_UPDATE_DISPLAY_NAME:
                break;

            default:
                return;
        }
    }

    @Override
    public void render() {
        ImGui.inputText("Target Username", this.targetUsername);
        ImGui.inputText("New Username", this.newUsername);
        ImGui.text("Target username is the username of\nthe player you want to change");
    }
}
