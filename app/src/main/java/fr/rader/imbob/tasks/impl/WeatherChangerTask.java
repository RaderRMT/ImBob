package fr.rader.imbob.tasks.impl;

import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.annotations.Task;
import fr.rader.imbob.types.VarInt;
import imgui.ImGui;
import imgui.type.ImInt;

@Task("Weather Changer")
public class WeatherChangerTask extends AbstractTask {

    private static final int BEGIN_RAIN           = 1;
    private static final int END_RAIN             = 2;
    private static final int RAIN_LEVEL_CHANGE    = 7;
    private static final int THUNDER_LEVEL_CHANGE = 8;

    private static final String[] WEATHERS = new String[] {
            "Clear",
            "Rain",
            "Thunder"
    };

    private final ImInt selectedWeather;

    public WeatherChangerTask() {
        this.selectedWeather = new ImInt(0);

        acceptPacket(PacketAcceptor.accept(Packets.get("change_game_state")));
        // we accept the Join Game packet because we will insert a few
        // Change Game State packets after it if the user decides
        // to change the weather to either rain or thunder
        acceptPacket(PacketAcceptor.accept(Packets.get("join_game")));
    }

    @Override
    public void execute(Packet packet, Queue<Packet> packets) {
        String newWeather = WEATHERS[this.selectedWeather.get()];
        Protocol protocolVersion = packet.getProtocol();

        switch (packet.getPacketName()) {
            case "join_game":
                // we don't add any packet after the join game packet
                // if we set the weather to clear in the replay
                if (newWeather.equals("Clear")) {
                    return;
                }

                // if we're here, this means the user wants to change the weather to either rain or thunder.
                // as both require a rain level of 20, i first create a Change Game State packet to begin the rain,
                // and then i create a second Change Game State packet to set the rain level to 1f
                Packet beginRainPacket = new Packet(
                        protocolVersion,
                        new VarInt(
                                Packets.get("change_game_state").getPacketIdForProtocol(protocolVersion)
                        ),
                        false
                );

                // we add the Begin Rain reason, with a 0f value.
                // the value doesn't matter, it can be anything
                beginRainPacket.add("reason", BEGIN_RAIN);
                beginRainPacket.add("value", 0f);

                // we clone an empty version of the beginRainPacket packet
                Packet setRainLevelPacket = beginRainPacket.cloneEmpty();
                // we add the Rain Level Change reason, with a 1f value.
                // 1f means the rain is at it's full intensity.
                setRainLevelPacket.add("reason", RAIN_LEVEL_CHANGE);
                setRainLevelPacket.add("value", 1f);

                // we add the packets to the queue with the correct order
                packets.add(beginRainPacket);
                packets.add(setRainLevelPacket);

                // if the user wants to change the weather to thunder, we just create
                // a third Change Game State packet and set the thunder level to 1f
                if (newWeather.equals("Thunder")) {
                    Packet setThunderLevelPacket = beginRainPacket.cloneEmpty();
                    setThunderLevelPacket.add("reason", THUNDER_LEVEL_CHANGE);
                    setThunderLevelPacket.add("value", 1f);

                    // we add the packet to the queue
                    packets.add(setThunderLevelPacket);
                }
                break;

            case "change_game_state":
                // we get the Change Game State packet's reason entry
                int reason = packet.get("reason", Integer.class);

                // we return if the packet doesn't affect the weather
                if (
                        reason != BEGIN_RAIN &&
                        reason != END_RAIN &&
                        reason != RAIN_LEVEL_CHANGE &&
                        reason != THUNDER_LEVEL_CHANGE
                ) {
                    return;
                }

                // otherwise, we just remove the packet from
                // the queue so it doesn't get written
                packets.poll();
                break;
        }
    }

    @Override
    public void render() {
        ImGui.pushItemWidth(150);
        ImGui.combo("New Weather", this.selectedWeather, WEATHERS);
        ImGui.popItemWidth();
    }
}
