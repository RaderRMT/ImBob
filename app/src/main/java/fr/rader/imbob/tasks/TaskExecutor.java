package fr.rader.imbob.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
        Collections.sort(tasks, (o1, o2) -> o2.getPriority() - o1.getPriority());

        System.out.println(tasks);

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
     * @throws IOException
     */
    private void applyTaskEdits(File replay) {
        this.progressBar.setLabel("Editing " + replay.getName() + "...");
        // clearing the packet definitions list so
        // we can chain multiple applyTaskEdits calls
        this.packetDefinitions.clear();

        try {
            ZipReader zipReader = new ZipReader(replay);
            ZipWriter zipWriter = new ZipWriter(replay);

            ReplayMetaData metaData = new ReplayMetaData(zipReader.getEntryAsStream("metaData.json"));

            DataReader reader = new DataReader(zipReader.getEntryAsStream("recording.tmcpr"));
            DataWriter writer = new DataWriter(zipWriter.createEntry("recording.tmcpr"));



            // edit logic



            // we ignore the first packet
            writer.writeInt(reader.readInt());
            int size = reader.readInt();
            writer.writeInt(size);
            writer.writeByteArray(reader.readFollowingBytes(size));

            PacketSerializer serializer = new PacketSerializer();
            PacketDeserializer deserializer = new PacketDeserializer();
            deserializer.setDataReader(reader);

            Queue<Packet> packets = new LinkedList<>();

            ProtocolVersion protocolVersion = metaData.getProtocol();

            while (reader.hasNext()) {
                int timestamp = reader.readInt();
                int packetSize = reader.readInt();
                VarInt packetId = reader.readVarInt();

                this.progressBar.setProgress((float) timestamp / (float) metaData.getDuration());

                Packet packet = new Packet(protocolVersion, packetId);
                for (AbstractTask task : this.tasks) {
                    if (!task.accept(packet)) {
                        continue;
                    }

                    if (packet.isEmpty()) {
                        PacketDefinition definition = getPacketDefinition(protocolVersion, packetId);
                        deserializer.deserialize(definition, packet);
                    }

                    packets.add(packet.clone());
                    task.execute(packet, packets);
                }

                if (packet.isEmpty()) {
                    writer.writeInt(timestamp);
                    writer.writeInt(packetSize);
                    writer.writeVarInt(packetId.getValue());
                    writer.writeByteArray(reader.readFollowingBytes(packetSize - packetId.size()));
                } else {
                    while (!packets.isEmpty()) {
                        Packet packetToWrite = packets.poll();
                        VarInt packetIdToWrite = packetToWrite.getPacketId();

                        PacketDefinition definition = getPacketDefinition(protocolVersion, packetIdToWrite);
                        serializer.serialize(definition, packetToWrite);
                        List<Byte> data = serializer.getData();

                        writer.writeInt(timestamp);
                        writer.writeInt(data.size() + packetIdToWrite.size());
                        writer.writeVarInt(packetIdToWrite.getValue());
                        writer.writeByteArray(ListUtils.toByteArray(data));
                    }
                }
            }





            // end edit logic

            writer.flush();
            zipWriter.closeEntry();

            zipReader.dumpToZipWriter(zipWriter);

            zipWriter.close();

            reader.close();
            zipReader.close();

            zipWriter.move();
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
