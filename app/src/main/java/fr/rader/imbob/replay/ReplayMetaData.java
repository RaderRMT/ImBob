package fr.rader.imbob.replay;

import java.io.InputStream;

import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.utils.json.JsonUtils;

public class ReplayMetaData {

    /** Whether the recording has been
     * recorded in a singleplayer world */
    private boolean singleplayer;

    /** The world name if the replay has been recorded
     * in singleplayer, or the server's ip address */
    private String serverName;

    /** The world name if the replay has been recorded
     * in singleplayer, or the server name as configured
     * in Minecraft's Add Server menu.
     * This can be absent on older replays */
    private String customServerName;

    /** The duration of the replay in milliseconds */
    private int duration;

    /** The unix timestamp in milliseconds
     * of when the recording was started */
    private long date;

    /** The Minecraft version */
    private String mcversion;

    /** The replay file format */
    private String fileFormat;

    /** The version of the replay file format */
    private int fileFormatVersion;

    /** The Minecraft protocol version */
    private int protocol;

    /** This is the name of the program
     * that generated the replay file */
    private String generator;

    /** The entity id of the player that recorded the replay */
    private int selfId;

    /** An array containing the UUIDs of every
     * player we can see in the replay */
    private String[] players;

    public static ReplayMetaData from(final InputStream inputStream) {
        return JsonUtils.fromStream(inputStream, ReplayMetaData.class);
    }

    public boolean isSingleplayer() {
        return this.singleplayer;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getCustomServerName() {
        return this.customServerName;
    }

    public int getDuration() {
        return this.duration;
    }

    public long getDate() {
        return this.date;
    }

    public String getMcVersion() {
        return this.mcversion;
    }

    public String getFileFormat() {
        return this.fileFormat;
    }

    public int getFileFormatVersion() {
        return this.fileFormatVersion;
    }

    public Protocol getProtocol() {
        return ProtocolVersion.getInstance().getFromId(this.protocol);
    }

    public String getGenerator() {
        return this.generator;
    }

    public int getSelfId() {
        return this.selfId;
    }

    public String[] getPlayers() {
        return this.players;
    }
}
