package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.annotations.Task;
import fr.rader.imbob.types.nbt.TagCompound;
import fr.rader.imbob.types.nbt.TagList;
import fr.rader.imbob.types.nbt.TagLong;
import fr.rader.imbob.utils.MathUtils;
import imgui.ImGui;
import imgui.type.ImInt;

@Task("Time Changer")
public class TimeChangerTask extends AbstractTask {

    private static final int TICKS_PER_DAY = 24_000;
    private static final int NOON = 6_000;

    private final ImInt timeOfDay;

    public TimeChangerTask() {
        this.timeOfDay = new ImInt(NOON);

        acceptPacket(PacketAcceptor.accept(Packets.get("time_update")));
        // we accept the Join Game packet starting from MC 1.16 because
        // it's at this version that the Join Game packet contains the
        // fixed time field if the world/server has the doDaylightCycle gamerule set to false
        acceptPacket(PacketAcceptor.accept(Packets.get("join_game")).from(ProtocolVersion.getInstance().get("MC_1_16")));

        // on 1.20.2+, the dimension codec has been moved to a packet
        acceptPacket(PacketAcceptor.accept(Packets.get("registry_data"), true).from(ProtocolVersion.getInstance().get("MC_1_20_2")));
    }

    @Override
    public void execute(Packet packet, Queue<Packet> packets) {
        // turn the time of day to a negative value between 0 and -24000
        long newTimeOfDay = Math.abs(this.timeOfDay.get()) % TICKS_PER_DAY;

        switch (packet.getPacketName()) {
            case "time_update": {
                // changing the time just means we need to change the time of day entry
                packet.update("time_of_day", -newTimeOfDay);
                break;
            }

            case "join_game": {
                if (packet.getProtocol().isAfterInclusive(ProtocolVersion.getInstance().get("MC_1_20_2"))) {
                    return;
                }

                // if the world/server has the doDaylightCycle set to false,
                // then we have to edit the dimension codec field
                // by adding or editing the "fixed_time" field
                TagCompound dimensionCodecCompound = packet.get("dimension_codec", TagCompound.class);
                patchDimensionCodec(dimensionCodecCompound, newTimeOfDay);

                break;
            }

            case "registry_data": {
                // if the world/server has the doDaylightCycle set to false,
                // then we have to edit the registry codec field
                // by adding or editing the "fixed_time" field

                TagCompound registryCodec = packet.get("registry_codec", TagCompound.class);
                patchDimensionCodec(registryCodec, newTimeOfDay);

                break;
            }
        }
    }

    private void patchDimensionCodec(TagCompound registryCodec, long newTimeOfDay) {
        TagCompound dimensionTypeRegistry = registryCodec.get("minecraft:dimension_type").getAsTagCompound();
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
