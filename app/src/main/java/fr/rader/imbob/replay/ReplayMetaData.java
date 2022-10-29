package fr.rader.imbob.replay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.utils.JsonUtils;

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
    private ProtocolVersion protocol;

    /** This is the name of the program
     * that generated the replay file */
    private String generator;

    /** The entity id of the player that recorded the replay */
    private int selfId;

    /** An array containing the UUIDs of every
     * player we can see in the replay */
    private String[] players;

    private final InputStream metaDataInputStream;

    public ReplayMetaData(InputStream metaDataInputStream) {
        this.metaDataInputStream = metaDataInputStream;

        readMetaData();
    }

    private void readMetaData() {
        try (InputStreamReader reader = new InputStreamReader(this.metaDataInputStream)) {
            Gson gson = new Gson();

            JsonObject metaDataRoot = gson.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : metaDataRoot.entrySet()) {
                JsonElement value = entry.getValue();

                switch (entry.getKey()) {
                    case "singleplayer":        this.singleplayer = value.getAsBoolean(); break;
                    case "serverName":          this.serverName = value.getAsString(); break;
                    case "customServerName":    this.customServerName = value.getAsString(); break;
                    case "duration":            this.duration = value.getAsInt(); break;
                    case "date":                this.date = value.getAsLong(); break;
                    case "mcversion":           this.mcversion = value.getAsString(); break;
                    case "fileFormat":          this.fileFormat = value.getAsString(); break;
                    case "fileFormatVersion":   this.fileFormatVersion = value.getAsInt(); break;
                    case "protocol":            this.protocol = ProtocolVersion.getProtocolFromId(value.getAsInt()); break;
                    case "generator":           this.generator = value.getAsString(); break;
                    case "selfId":              this.selfId = value.getAsInt(); break;
                    case "players":             this.players = JsonUtils.getAsStringArray(value.getAsJsonArray()); break;
                    default: break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String getMcversion() {
        return this.mcversion;
    }

    public String getFileFormat() {
        return this.fileFormat;
    }

    public int getFileFormatVersion() {
        return this.fileFormatVersion;
    }

    public ProtocolVersion getProtocol() {
        return this.protocol;
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

    public InputStream getMetaDataInputStream() {
        return this.metaDataInputStream;
    }
}
