package fr.rader.imbob.tasks;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import bsh.EvalError;
import bsh.Interpreter;
import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.Packets;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.replay.ReplayMetaData;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.utils.data.DataReader;
import fr.rader.imbob.utils.data.DataWriter;
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

    private final Interpreter interpreter;

    public TaskExecutor(List<AbstractTask> tasks, List<File> replays, ProgressBarWindow progressBar) {
        tasks.sort((o1, o2) -> o2.getPriority() - o1.getPriority());

        this.tasks = tasks;
        this.replays = replays;
        this.progressBar = progressBar;

        this.interpreter = new Interpreter();
        this.interpreter.setStrictJava(true);
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.VarInt");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.VarLong");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.Position");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagBase");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagByte");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagByteArray");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagCompound");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagDouble");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagFloat");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagInt");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagIntArray");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagList");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagLong");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagLongArray");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagShort");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.types.nbt.TagString");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.packets.data.Data");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.packets.data.DataBlock");
        this.interpreter.getNameSpace().importClass("fr.rader.imbob.packets.data.DataBlockArray");
    }

    /**
     * Apply all the tasks to the replays.
     * This will lock the user inputs and start editing
     * in a separate thread, one replay at a time.
     * Once editing is done, user inputs will be unlocked.
     */
    public void applyAllTaskEdits() {
        new Thread(() -> {
            ImGuiIO io = ImGui.getIO();
            // disable any user inputs
            io.setConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard | ImGuiConfigFlags.NoMouse);

            // show the progress bar
            this.progressBar.setVisible(true);
            // do the edits
            this.replays.forEach(this::applyTaskEdits);

            returnUserControl();
        }).start();
    }

    /**
     * Apply all edits to a single replay file.
     * This method is private because applyAllTaskEdits can be used for a single replay
     *
     * @param replay    The replay to edit
     */
    private void applyTaskEdits(File replay) {
        this.progressBar.setProgress(0f);
        this.progressBar.setLabel("Editing " + replay.getName() + "...");

        try (
                ZipReader zipReader = new ZipReader(replay);
                DataReader reader = new DataReader(zipReader.getEntryAsStream("recording.tmcpr"))
        ) {
            ZipWriter zipWriter = new ZipWriter(replay);
            DataWriter writer = new DataWriter(zipWriter.createEntry("recording.tmcpr"));

            ignoreLoginSuccess(reader, writer);

            // give BeanShell the DataReader instance, so we can
            // read data from the recording in packet definition scripts
            this.interpreter.set("reader", reader);

            ReplayMetaData metaData = ReplayMetaData.from(zipReader.getEntryAsStream("metaData.json"));
            if (metaData.getProtocol() == null) {
                LoggerWindow.warn(replay.getName() + " cannot be edited because its protocol isn't supported");
                return;
            }

            Protocol protocol = metaData.getProtocol();

            Queue<Packet> packets = new LinkedList<>();

            while (reader.hasNext()) {
                int timestamp = reader.readInt();
                int packetSize = reader.readInt();
                VarInt packetId = reader.readVarInt();

                this.progressBar.setProgress((float) timestamp / (float) metaData.getDuration());

                Packet packet = new Packet(protocol, packetId);
                List<AbstractTask> tasks = this.tasks.stream()
                        .filter(task -> task.accept(packet))
                        .collect(Collectors.toList());

                for (AbstractTask task : tasks) {
                    if (packet.isEmpty()) {
                        this.interpreter.set("packet", packet);

                        packet.setPacketName(Packets.get(protocol, packet.getPacketId().get()).getName());
                        deserializePacket(packet);
                    }

                    packets.add(packet);
                    task.execute(packet, packets);
                }

                if (packet.isEmpty()) {
                    writer.writeInt(timestamp);
                    writer.writeInt(packetSize);
                    writer.writeVarInt(packetId.get());
                    writer.writeByteArray(reader.readFollowingBytes(packetSize - packetId.size()));
                } else {
                    writePackets(timestamp, packets, writer);
                }
            }

            writer.flush();
            zipWriter.closeEntry();

            zipReader.dumpToZipWriter(zipWriter);

            zipWriter.close();

            zipWriter.move();
        } catch (IOException | EvalError e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void writePackets(final int timestamp, final Queue<Packet> packets, final DataWriter writer) {
        while (!packets.isEmpty()) {
            writePacket(
                    timestamp,
                    packets.poll(),
                    writer
            );
        }
    }

    private void writePacket(final int timestamp, final Packet packet, final DataWriter recordingWriter) {
        try (DataWriter writer = new DataWriter()) {
            this.interpreter.set("writer", writer);
            this.interpreter.set("packet", packet);

            this.interpreter.source(Packets.getPSLWritePath(
                    packet.getProtocol(),
                    packet.getPacketId().get()
            ));

            recordingWriter.writeInt(timestamp);
            recordingWriter.writeInt(writer.getData().size() + packet.getPacketId().size());
            recordingWriter.writeVarInt(packet.getPacketId().get());
            recordingWriter.writeByteList(writer.getData());
        } catch (IOException | EvalError e) {
            e.printStackTrace();
        }
    }

    private void deserializePacket(Packet packet) {
        try {
            this.interpreter.source(Packets.getPSLReadPath(
                    packet.getProtocol(),
                    packet.getPacketId().get()
            ));
        } catch (IOException | EvalError e) {
            e.printStackTrace();
        }
    }

    private void ignoreLoginSuccess(final DataReader reader, final DataWriter writer) {
        VarInt packetId;

        do {
            // timestamp
            writer.writeInt(reader.readInt());

            int size = reader.readInt();
            writer.writeInt(size);

            packetId = reader.readVarInt();
            writer.writeVarInt(packetId.get());

            writer.writeByteArray(reader.readFollowingBytes(size - packetId.size()));
        } while (packetId.get() != 0x02); // todo: not hardcode 0x02
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
