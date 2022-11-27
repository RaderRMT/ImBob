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
import fr.rader.imbob.utils.MojangAPI;
import fr.rader.imbob.windows.impl.LoggerWindow;
import imgui.ImGui;
import imgui.type.ImString;

@Task(
    value = "Skin Changer",
    // we set the priority to a high level because we want
    // to change the skin before changing the username,
    // so we can use the same username for both tasks
    priority = 100
)
public class SkinChangerTask extends AbstractTask {

    private static final int ACTION_ADD_PLAYER = 0;

    private static String[] cachedSkinData;

    private final ImString targetUsername;
    private final ImString fetchValue;

    public SkinChangerTask() {
        this.targetUsername = new ImString();
        this.fetchValue = new ImString();

        acceptPacket(PacketAcceptor.accept(Packets.PLAYER_INFO));
    }

    @Override
    protected void execute(Packet packet, Queue<Packet> packets) {
        int action = packet.getEntry("action")
                           .getAs(VariableEntry.class)
                           .getValueAs(VarInt.class)
                           .getValue();

        // we only edit packets with the ACTION_ADD_PLAYER action because
        // the other actions don't have the textures property field
        if (action != ACTION_ADD_PLAYER) {
            return;
        }

        // we get the skin data from mojang's servers
        if (cachedSkinData == null) {
            cachedSkinData = MojangAPI.getSkinData(this.fetchValue.get());
        }

        ArrayEntry players = packet.getEntry("players").getAs(ArrayEntry.class);
        for (EntryList player : players) {
            // we loop through each players in this packet and
            // we skip the user if it isn't the one we want to edit
            if (
                    !player.get("name")
                           .getAs(VariableEntry.class)
                           .getValueAs(String.class)
                           .equals(this.fetchValue.get())
            ) {
                continue;
            }

            // if the user doesn't exist, we print an error message and return
            if (cachedSkinData == null) {
                LoggerWindow.error("User \"" + this.fetchValue.get() + "\" does not exist.");
                return;
            }

            ArrayEntry properties = player.get("properties").getAs(ArrayEntry.class);

            // we look through each properties for the textures property
            properties.forEach(property -> {
                if (property.get("name").getAs(VariableEntry.class).getValueAs(String.class).equals("textures")) {
                    property.get("value").getAs(VariableEntry.class).setValue(cachedSkinData[0]);
                    property.get("signature").getAs(VariableEntry.class).setValue(cachedSkinData[1]);
                }
            });
        }
    }

    @Override
    public void render() {
        ImGui.inputText("Target", this.targetUsername);
        ImGui.inputText("Skin Player", this.fetchValue);

        ImGui.text("Warning: an internet connection is\nrequired for this task to work.");
        ImGui.text("Skin Player must be the username of a\nplayer that has the skin you want\nto change to.");
    }
}
