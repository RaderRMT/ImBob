package fr.rader.imbob.types;

public class VarInt {

    private int value;

    public VarInt(int value) {
        this.value = value;
    }

    public int get() {
        return this.value;
    }

    public void set(int value) {
        this.value = value;
    }

    public int size() {
        int bytes = 1;

        int temp = this.value;
        while ((temp >>>= 7) != 0) {
            bytes++;
        }

        return bytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        VarInt temp = (VarInt) obj;
        return this.value == temp.value;
    }
}
