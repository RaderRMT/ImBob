package fr.rader.imbob.utils.data;

import fr.rader.imbob.types.Position;
import fr.rader.imbob.types.VarInt;
import fr.rader.imbob.types.VarLong;
import fr.rader.imbob.types.nbt.TagCompound;
import fr.rader.imbob.windows.impl.LoggerWindow;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.protocol.ProtocolVersion;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class DataReader implements AutoCloseable {

    private InputStream inputStream;

    public DataReader(File file) {
        if (file == null) {
            LoggerWindow.error("File is null!");
            return;
        }

        if (!file.exists()) {
            LoggerWindow.error("File does not exist: " + file.getAbsolutePath());
            return;
        }

        if (!file.isFile()) {
            LoggerWindow.error("File is a directory: " + file.getAbsolutePath());
            return;
        }

        try {
            this.inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public DataReader(InputStream inputStream) {
        if (inputStream == null) {
            LoggerWindow.error("InputStream is null!");
            return;
        }

        this.inputStream = inputStream;
    }

    public boolean readBoolean() {
        return readByte() != 0;
    }

    public int readByte() {
        if (this.inputStream == null) {
            throw new IllegalStateException("InputStream is null!");
        }

        try {
            if (this.inputStream.available() == 0) {
                throw new EOFException("Reached end of file!");
            }

            return this.inputStream.read() & 0xFF;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int readShort() {
        return (readByte() << 8 | readByte()) & 0xffff;
    }

    public int readInt() {
        return readShort() << 16 | readShort();
    }

    public long readLong() {
        return (long) readInt() << 32 | readInt() & 0xffffffffL;
    }

    public float readFloat() {
        return ByteBuffer.wrap(readFollowingBytes(4)).order(ByteOrder.BIG_ENDIAN).getFloat();
    }

    public double readDouble() {
        return ByteBuffer.wrap(readFollowingBytes(8)).order(ByteOrder.BIG_ENDIAN).getDouble();
    }

    public char readChar() {
        return (char) readByte();
    }

    public byte[] readFollowingBytes(int length) {
        byte[] out = new byte[length];

        for (int i = 0; i < length; i++) {
            out[i] = (byte) readByte();
        }

        return out;
    }

    public VarInt readVarInt() {
        int bytesRead = 0;
        int result = 0;
        int read;

        do {
            read = readByte();
            int value = (read & 0x7f);
            result |= value << (7 * bytesRead);

            bytesRead++;
            if (bytesRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0x80) != 0);

        return new VarInt(result);
    }

    public VarLong readVarLong() {
        int numRead = 0;
        long result = 0;
        int read;

        do {
            read = readByte();
            long value = (read & 0x7f);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0x80) != 0);

        return new VarLong(result);
    }

    public String readString() {
        return readString(readVarInt().get());
    }

    public String readString(int length) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < length; i++) {
            out.append(readChar());
        }

        return out.toString();
    }

    public int[] readIntArray(int length) {
        int[] out = new int[length];

        for (int i = 0; i < length; i++) {
            out[i] = readInt();
        }

        return out;
    }

    public long[] readLongArray(int length) {
        long[] out = new long[length];

        for (int i = 0; i < length; i++) {
            out[i] = readLong();
        }

        return out;
    }

    public TagCompound readNBT() {
        byte firstByte = (byte) readByte();
        if (firstByte == 0) {
            return null;
        }

        return new TagCompound(readString(readShort()), this);
    }

    public UUID readUUID() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(readFollowingBytes(16));
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public Position readPosition(Protocol protocolVersion) {
        long positionValue = readLong();

        int x;
        int y;
        int z;

        if (protocolVersion.isBeforeExclusive(ProtocolVersion.getInstance().get("MC_1_14"))) {
            x = (int) (positionValue >> 38);
            y = (int) (positionValue >> 26) & 0xfff;
            z = (int) (positionValue << 38 >> 38);
        } else {
            x = (int) (positionValue >> 38);
            y = (int) (positionValue << 52 >> 52);
            z = (int) (positionValue << 38 >> 38);
        }

        return new Position(protocolVersion, x, y, z);
    }

    public boolean hasNext() {
        try {
            return this.inputStream.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getLength() {
        try {
            return this.inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void skip(int i) {
        try {
            this.inputStream.skip(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.inputStream == null) {
            return;
        }

        this.inputStream.close();
    }
}
