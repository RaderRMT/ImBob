package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.psl.packets.serialization.entries.VariableEntry;
import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.annotations.TaskName;
import fr.rader.imbob.types.nbt.TagCompound;
import fr.rader.imbob.types.nbt.TagList;
import fr.rader.imbob.types.nbt.TagLong;
import fr.rader.imbob.utils.MathUtils;
import imgui.ImGui;
import imgui.type.ImInt;

@TaskName("Time Changer")
public class TimeChangerTask extends AbstractTask {

    private static final int TICKS_PER_DAY = 24_000;
    private static final int NOON = 6_000;

    private final ImInt timeOfDay;

    public TimeChangerTask() {
        this.timeOfDay = new ImInt(NOON);

        acceptPacket(PacketAcceptor.accept(Packets.TIME_UPDATE));
        // we accept the Join Game packet starting from MC 1.16 because
        // it's at this version that the Join Game packet contains the
        // fixed time field if the world/server has the doDaylightCycle gamerule set to false
        acceptPacket(PacketAcceptor.accept(Packets.JOIN_GAME).from(ProtocolVersion.MC_1_16));
    }

    @Override
    public void execute(Packet packet, Queue<Packet> packets) {
        // turn the time of day to a negative value between 0 and -24000
        long newTimeOfDay = -(Math.abs(this.timeOfDay.get()) % TICKS_PER_DAY);

        switch (packet.getPacketName()) {
            case "time_update":
                // changing the time just means we need to change the time of day entry
                packet.getEntry("time_of_day")
                      .getAs(VariableEntry.class)
                      .setValue(newTimeOfDay);
                break;

            case "join_game":
                // if the world/server has the doDaylightCycle set to false,
                // then we have to edit the dimension codec field
                // by adding or editing the "fixed_time" field
                VariableEntry dimensionCodec = packet.getEntry("dimension_codec").getAs(VariableEntry.class);

                TagCompound dimensionCodecCompound = dimensionCodec.getValueAs(TagCompound.class);
                TagCompound dimensionTypeRegistry = dimensionCodecCompound.get("minecraft:dimension_type").getAsTagCompound();
                TagList<TagCompound> value = dimensionTypeRegistry.get("value").getAsCompoundList();

                // we add the fixed_time field to every dimension because why not
                for (TagCompound compound : value) {
                    TagCompound element = compound.get("element").getAsTagCompound();

                    if (!element.has("fixed_time")) {
                        element.add(new TagLong("fixed_time", newTimeOfDay));
                    } else {
                        element.get("fixed_time").getAsTagLong().setValue(newTimeOfDay);
                    }
                }

                break;
        }
    }

    @Override
    public void render() {
        // we clamp the value between 0 and 24000 ticks
        this.timeOfDay.set(MathUtils.clamp(this.timeOfDay.get(), 0, TICKS_PER_DAY));

        ImGui.pushItemWidth(150);
        ImGui.inputInt("Time Of Day", this.timeOfDay);
        ImGui.popItemWidth();
        ImGui.text("This uses the same value\nas the \"/time set\" command");
    }
}
