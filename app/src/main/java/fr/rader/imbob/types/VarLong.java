package fr.rader.imbob.types;

public class VarLong {

    private long value;

    public VarLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int size() {
        int bytes = 1;

        long temp = value;
        while ((temp >>>= 7) != 0) {
            bytes++;
        }

        return bytes;
    }
}
