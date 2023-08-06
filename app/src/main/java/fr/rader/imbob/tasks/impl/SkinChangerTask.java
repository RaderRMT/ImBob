package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.packets.data.DataBlock;
import fr.rader.imbob.packets.data.DataBlockArray;
import fr.rader.imbob.protocol.ProtocolVersion;
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
    private static final int ACTION_ADD_PLAYER_BIT = 0x01;

    private final ImString targetUsername;
    private final ImString fetchValue;

    private String[] cachedSkinData;
    private boolean hasSkinBeenChanged;

    public SkinChangerTask() {
        this.targetUsername = new ImString();
        this.fetchValue = new ImString();
        this.hasSkinBeenChanged = false;

        acceptPacket(PacketAcceptor.accept(Packets.get("player_info")));
    }

    private void edit761(final Packet packet) {
        int actionBitfield = packet.get("action", Integer.class);

        // we only edit packets with the ACTION_ADD_PLAYER action because
        // the other actions don't have the textures property field
        if ((actionBitfield & ACTION_ADD_PLAYER_BIT) == 0) {
            return;
        }

        // we get the skin data from mojang's servers
        if (this.cachedSkinData == null) {
            this.cachedSkinData = MojangAPI.getSkinData(this.fetchValue.get());
        }

        DataBlockArray players = packet.get("actions", DataBlockArray.class);
        for (DataBlock action : players) {
            if (!action.contains("name")) {
                continue;
            }

            // we loop through each players in this packet and
            // we skip the user if it isn't the one we want to edit
            if (!action.get("name", String.class).equals(this.targetUsername.get())) {
                continue;
            }

            // if the user doesn't exist, we print an error message and return
            if (this.cachedSkinData == null) {
                LoggerWindow.error("User \"" + this.fetchValue.get() + "\" does not exist.");
                return;
            }

            DataBlockArray properties = action.get("properties", DataBlockArray.class);

            // we look through each properties for the textures property
            properties.forEach(property -> {
                if (property.get("name", String.class).equals("textures")) {
                    property.get("value").setValue(this.cachedSkinData[0]);
                    property.get("signature").setValue(this.cachedSkinData[1]);

                    this.hasSkinBeenChanged = true;
                }
            });

            if (!this.hasSkinBeenChanged) {
                DataBlock texturesEntryList = properties.create();
                texturesEntryList.add("name", "textures");
                texturesEntryList.add("value", this.cachedSkinData[0]);
                texturesEntryList.add("is_signed", true);
                texturesEntryList.add("signature", this.cachedSkinData[1]);
            }
        }

        // resetting the boolean to false so we're prepared for a possible next edit
        this.hasSkinBeenChanged = false;
    }

    private void edit(final Packet packet) {
        int action = packet.get("action", VarInt.class).get();

        // we only edit packets with the ACTION_ADD_PLAYER action because
        // the other actions don't have the textures property field
        if (action != ACTION_ADD_PLAYER) {
            return;
        }

        // we get the skin data from mojang's servers
        if (this.cachedSkinData == null) {
            this.cachedSkinData = MojangAPI.getSkinData(this.fetchValue.get());
        }

        DataBlockArray players = packet.get("players", DataBlockArray.class);
        for (DataBlock player : players) {
            // we loop through each players in this packet and
            // we skip the user if it isn't the one we want to edit
            if (!player.get("name", String.class).equals(this.targetUsername.get())) {
                continue;
            }

            // if the user doesn't exist, we print an error message and return
            if (this.cachedSkinData == null) {
                LoggerWindow.error("User \"" + this.fetchValue.get() + "\" does not exist.");
                return;
            }

            DataBlockArray properties = player.get("properties", DataBlockArray.class);

            // we look through each properties for the textures property
            properties.forEach(property -> {
                if (property.get("name", String.class).equals("textures")) {
                    property.get("value").setValue(this.cachedSkinData[0]);
                    property.get("signature").setValue(this.cachedSkinData[1]);

                    this.hasSkinBeenChanged = true;
                }
            });

            if (!this.hasSkinBeenChanged) {
                DataBlock texturesEntryList = properties.create();
                texturesEntryList.add("name", "textures");
                texturesEntryList.add("value", this.cachedSkinData[0]);
                texturesEntryList.add("is_signed", true);
                texturesEntryList.add("signature", this.cachedSkinData[1]);

                VarInt numberOfProperties = player.get("number_of_properties", VarInt.class);
                numberOfProperties.set(numberOfProperties.get() + 1);
            }
        }

        // resetting the boolean to false so we're prepared for a possible next edit
        this.hasSkinBeenChanged = false;
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
        ImGui.inputText("Target", this.targetUsername);
        ImGui.inputText("Skin Player", this.fetchValue);

        ImGui.text("Warning: an internet connection is\nrequired for this task to work.");
        ImGui.text("Skin Player must be the username of a\nplayer that has the skin you want\nto change to.");
    }
}
