package fr.rader.imbob.utils.data;

import fr.rader.imbob.protocol.ProtocolVersion;
import fr.rader.imbob.types.Position;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.types.nbt.TagCompound;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataWriter implements AutoCloseable {

    private static final int BUFFER_SIZE = 16384;

    private final byte[] buffer = new byte[BUFFER_SIZE];

    private final OutputStream outputStream;

    private final List<Byte> data;

    private int index = 0;

    // use internal list
    public DataWriter() {
        this.outputStream = null;
        this.data = new ArrayList<>();
    }

    public DataWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.data = null;
    }

    public void writeByte(int value) {
        if (this.data != null) {
            this.data.add((byte) (value & 0xff));
            return;
        }

        if (this.index == BUFFER_SIZE) {
            flush();
        }

        this.buffer[this.index] = (byte) (value & 0xff);
        this.index++;
    }

    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    public void writeShort(int value) {
        writeByte(value >>> 8);
        writeByte(value & 0xff);
    }

    public void writeInt(int value) {
        writeShort(value >>> 16);
        writeShort(value & 0xffff);
    }

    public void writeLong(long value) {
        writeInt((int) (value >>> 32));
        writeInt((int) value);
    }

    public void writeByteArray(byte[] values) {
        for (byte value : values) {
            writeByte(value);
        }
    }

    public void writeByteList(List<Byte> values) {
        values.forEach(this::writeByte);
    }

    public void writeIntArray(int[] values) {
        for (int value : values) {
            writeInt(value);
        }
    }

    public void writeIntList(List<Integer> values) {
        values.forEach(this::writeInt);
    }

    public void writeLongArray(long[] values) {
        for (long value : values) {
            writeLong(value);
        }
    }

    public void writeLongList(List<Long> values) {
        values.forEach(this::writeLong);
    }

    public void writeFloat(float value) {
        writeByteArray(ByteBuffer.allocate(4).putFloat(value).array());
    }

    public void writeDouble(double value) {
        writeByteArray(ByteBuffer.allocate(8).putDouble(value).array());
    }

    public void writeVarInt(final VarInt value) {
        writeVarInt(value.get());
    }

    public void writeVarInt(int value) {
        do {
            byte temp = (byte) (value & 0x7f);
            value >>>= 7;

            if (value != 0) {
                temp |= 0x80;
            }

            writeByte(temp);
        } while (value != 0);
    }

    public void writeVarLong(long value) {
        do {
            byte temp = (byte) (value & 0x7f);
            value >>>= 7;

            if (value != 0) {
                temp |= 0x80;
            }

            writeByte(temp);
        } while (value != 0);
    }

    public void writeNBT(final TagCompound value) {
        value.write(this);
    }

    public void writePacketString(final String value) {
        writeVarInt(value.length());
        writeString(value);
    }

    public void writeString(String value) {
        writeByteArray(value.getBytes(StandardCharsets.UTF_8));
    }

    public void writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writePosition(Position position) {
        if (position.getProtocol().isBeforeExclusive(ProtocolVersion.getInstance().get("MC_1_14"))) {
            writeLong(
                    ((long) (position.getX() & 0x3ffffff) << 38) |
                    ((long) (position.getY() & 0xfff) << 26) |
                    (position.getZ() & 0x3ffffff)
            );
        } else {
            writeLong(
                    ((long) (position.getX() & 0x3ffffff) << 38) |
                    ((long) (position.getZ() & 0x3ffffff) << 12) |
                    (position.getY() & 0xfff)
            );
        }
    }

    public void flush() {
        if (this.outputStream == null) {
            return;
        }

        try {
            this.outputStream.write(this.buffer, 0, this.index);
            this.outputStream.flush();

            this.index = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Byte> getData() {
        return this.data;
    }

    public int index() {
        return this.index;
    }

    @Override
    public void close() throws IOException {
        if (this.outputStream != null) {
            this.outputStream.close();
        }
    }
}
