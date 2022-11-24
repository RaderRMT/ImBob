package fr.rader.imbob.tasks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.psl.packets.definition.PacketDefinition;
import fr.rader.imbob.psl.packets.definition.PacketDefinitionFactory;
import fr.rader.imbob.psl.packets.serialization.PacketDeserializer;
import fr.rader.imbob.psl.packets.serialization.PacketSerializer;
import fr.rader.imbob.replay.ReplayMetaData;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.utils.data.DataReader;
import fr.rader.imbob.utils.data.DataWriter;
import fr.rader.imbob.utils.data.ListUtils;
import fr.rader.imbob.utils.zip.ZipReader;
import fr.rader.imbob.utils.zip.ZipWriter;
import fr.rader.imbob.windows.impl.LoggerWindow;
import fr.rader.imbob.windows.impl.ProgressBarWindow;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;

public class TaskExecutor {

    private final ProgressBarWindow progressBar;

    private final List<AbstractTask> tasks;
    private final List<File> replays;

    private final Map<Integer, PacketDefinition> packetDefinitions;

    public TaskExecutor(List<AbstractTask> tasks, List<File> replays, ProgressBarWindow progressBar) {
        this.tasks = tasks;
        this.replays = replays;
        this.progressBar = progressBar;

        this.packetDefinitions = new HashMap<>();
    }

    /**
     * Apply all the tasks to the replays.
     * This will lock the user inputs and start editing
     * in a separate thread, one replay at a time.
     * Once editing is done, user inputs will be unlocked.
     */
    public void applyAllTaskEdits() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImGuiIO io = ImGui.getIO();
                // disable any user inputs
                io.setConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard | ImGuiConfigFlags.NoMouse);

                // show the progress bar
                progressBar.setVisible(true);
                // do the edits
                replays.forEach(replay -> {
                    progressBar.setProgress(0f);
                    applyTaskEdits(replay);
                });

                returnUserControl();
            }
        }).start();
    }

    /**
     * Apply all edits to a single replay file.
     * This method is private because applyAllTaskEdits can be used for a single replay
     *
     * @param replay    The replay to edit
     */
    private void applyTaskEdits(File replay) {
        this.progressBar.setLabel("Editing " + replay.getName() + "...");
        // clearing the packet definitions list so
        // we can chain multiple applyTaskEdits calls
        this.packetDefinitions.clear();

        // we open the replay file as a zip
        ZipReader zipReader = new ZipReader(replay);
        // and we get the metadata from the zip
        ReplayMetaData metaData = new ReplayMetaData(zipReader.getEntry("metaData.json"));

        try {
            // we open the recording in a datareader, it's a stream on steroid
            DataReader replayReader = new DataReader(zipReader.getEntry("recording.tmcpr"));
            // and we make a data writer so we can write
            // the packets to a new recording file
            DataWriter writer = new DataWriter(true);

            // we ignore the first packet
            writer.writeInt(replayReader.readInt());    // this is the timestamp
            int size = replayReader.readInt();          // this is the packet size
            writer.writeInt(size);
            writer.writeByteArray(replayReader.readFollowingBytes(size)); // this is the packet data

            // the packet deserializer is used to, as its name suggest,
            // deserialize packets from the replayReader to a Packet
            PacketDeserializer deserializer = new PacketDeserializer();
            // we set the deserializer's data reader to the replayReader
            // so the packet deserializer knows where to read the data
            deserializer.setDataReader(replayReader);
            // the packet serializer is used to, as its name suggest,
            // serialize packets to a data writer
            PacketSerializer serializer = new PacketSerializer();
            // we create a queue for our packets, this will allow tasks
            // to clear the queue to not write any packet, or add
            // multiple packets to be added in the recording
            Queue<Packet> packets = new LinkedList<>();

            // we get the protocol version from the replay metadata
            ProtocolVersion protocolVersion = metaData.getProtocol();

            // we read the replayReader until we're at the end of the recording
            while (replayReader.getLength() != 0) {
                // we get the packet timestamp
                int timestamp = replayReader.readInt();
                // we get the packet size
                int packetSize = replayReader.readInt();
                // and we get the packet id
                VarInt packetId = replayReader.readVarInt();

                // at this point, we can update the progress bar.
                // we could've done it after reading timestamp but
                // i don't want to mix things together
                this.progressBar.setProgress((float) timestamp / (float) metaData.getDuration());

                // we create an empty packet that only contains the protocol version and the packet id.
                // each task will then get the packet and see if they can edit it based on those 2 values.
                Packet packet = new Packet(protocolVersion, packetId);
                // we loop through all the tasks
                for (AbstractTask task : this.tasks) {
                    // we ignore the packet if it's not accepted
                    if (!task.accept(packet)) {
                        continue;
                    }

                    // we fill the packet only if it is empty.
                    // this prevents us from reading garbage data if
                    // at least two tasks are used at once
                    if (packet.isEmpty()) {
                        // we get the packet definition for the packet we want to edit
                        PacketDefinition definition = getPacketDefinition(protocolVersion, packetId);
                        // and we deserialize the packet
                        deserializer.deserialize(definition, packet);
                    }

                    // we create a clone of the packet because we don't
                    // want to do edits based on a packet that
                    // might've been edited by a previous task.
                    // we then add that cloned packet to the packets queue
                    packets.add(packet.clone());
                    // we execute the task.
                    // we peek the packet in the queue because we want
                    // to edit the packet contained in the queue
                    task.execute(packets.peek(), packets);

                    // we loop through each packet we want to add in the replay.
                    // the queue can be empty or contain more than one packet.
                    while (!packets.isEmpty()) {
                        // we get the head of the queue
                        Packet packetToWrite = packets.poll();
                        VarInt packetIdToWrite = packetToWrite.getPacketId();

                        // we get the packet definition
                        PacketDefinition definition = getPacketDefinition(protocolVersion, packetIdToWrite);
                        // we serialize the packet back to a list of bytes
                        serializer.serialize(definition, packetToWrite);
                        // we get the serialized packet
                        List<Byte> data = serializer.getData();

                        // we write the timestamp to the data writer
                        writer.writeInt(timestamp);
                        // and we write all the data, so the size of the packet data
                        // + the size of the packet id, giving us the size of the entire packet
                        writer.writeInt(data.size() + packetIdToWrite.size());
                        // then we write the packet id
                        writer.writeVarInt(packetIdToWrite.getValue());
                        // and finally we write the packet data
                        writer.writeByteArray(ListUtils.toByteArray(data));
                    }
                }

                // we check if the packet has no entries so
                // we don't write it twice after editing it
                if (packet.isEmpty()) {
                    // if the packet has no entries, this means it hasn't been accepted by a task.
                    // we just have to rewrite its timestamp, packet size, id and data
                    writer.writeInt(timestamp);
                    writer.writeInt(packetSize);
                    writer.writeVarInt(packetId.getValue());
                    writer.writeByteArray(replayReader.readFollowingBytes(packetSize - packetId.size()));
                }
            }

            // we flush the buffer in the writer
            writer.flush();

            // we write a zip file for our recording
            ZipWriter zipWriter = new ZipWriter(replay);

            // we get the data writer as an input stream, so we can write
            // its content to a "recording.tmcpr" entry in the zip file
            zipWriter.addEntry("recording.tmcpr", writer.getInputStream());
            // then we can copy over all the missing files
            zipReader.dumpToZipWriter(zipWriter);

            // we close the zip reader
            zipReader.close();
            // we can close both the replay zip and writer's input stream
            zipWriter.close();

            // and we can delete the temp file generated by the data writer
            writer.clear();
        } catch (IOException e) {
            LoggerWindow.error("Error when editing " + replay.getName() + ": " + e.getMessage());
            returnUserControl();
        }
    }

    /**
     * Get or create a {@link PacketDefinition} based on the given {@link ProtocolVersion},
     * packet id and the current state of the packetDefinitions list.
     *
     * @param version   The replay {@link ProtocolVersion}
     * @param packetId  The packet id
     * @return          The {@link PacketDefinition} from the packetDefinitions list,
     *                  or a new {@link PacketDefinition} if it isn't contained in the list
     * @throws IOException If an I/O error occurs
     */
    private PacketDefinition getPacketDefinition(ProtocolVersion version, VarInt packetId) throws IOException {
        // we create a new packet definition if the list
        // doesn't contain the wanted packet definition
        if (!this.packetDefinitions.containsKey(packetId.getValue())) {
            this.packetDefinitions.put(
                    packetId.getValue(),
                    PacketDefinitionFactory.createPacketDefinition(
                            version,
                            packetId
                    )
            );
        }

        // we return the packet definition from the list
        return this.packetDefinitions.get(packetId.getValue());
    }

    private void returnUserControl() {
        // reset the progress bar back to 0%
        this.progressBar.setProgress(0f);

        // hide the progress bar at the end of the edit
        this.progressBar.setVisible(false);

        // enable the user inputs we disabled
        ImGui.getIO().removeConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard | ImGuiConfigFlags.NoMouse);
    }
}
