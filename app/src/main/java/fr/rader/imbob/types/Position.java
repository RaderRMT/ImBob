package fr.rader.imbob.types;

import fr.rader.imbob.protocol.Protocol;

public class Position {

    private final Protocol protocolVersion;

    private int x;
    private int y;
    private int z;

    public Position(Protocol protocolVersion, int x, int y, int z) {
        this.protocolVersion = protocolVersion;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Protocol getProtocol() {
        return this.protocolVersion;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
