package fr.rader.imbob.protocol;

public class Protocol {

    private final String name;
    private final int version;

    private Protocol() {
        this.name = "No Name";
        this.version = -1;
    }

    public String getName() {
        return this.name;
    }

    public int getVersion() {
        return this.version;
    }

/**
     * <pre>
     * Check if the given protocol version is newer or as new as the other version
     * For example, these two will return true:
     *
     *     Protocol.MC_1_19.isAfterInclusive(
     *              Protocol.MC_1_16_5
     *     );
     *
     *     Protocol.MC_1_14_2.isAfterInclusive(
     *              Protocol.MC_1_14_2
     *     );
     *
     * This one however will return false:
     *
     *     Protocol.MC_1_11_2.isAfterInclusive(
     *              Protocol.MC_1_15_1
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is newer or as old as the other version, false otherwise
    */
    public boolean isAfterInclusive(Protocol otherVersion) {
        return this.version >= otherVersion.getVersion();
    }

    /**
     * <pre>
     * Check if the given protocol version is newer than the other version
     * For example, these two will return true:
     *
     *     Protocol.MC_1_19.isAfterExclusive(
     *              Protocol.MC_1_16_5
     *     );
     *
     * These ones however will return false:
     *
     *     Protocol.MC_1_14_2.isAfterExclusive(
     *              Protocol.MC_1_14_2
     *     );
     *
     *     Protocol.MC_1_11_2.isAfterExclusive(
     *              Protocol.MC_1_15_1
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is newer than the other version, false otherwise
    */
    public boolean isAfterExclusive(Protocol otherVersion) {
        return this.version > otherVersion.getVersion();
    }

    /**
     * <pre>
     * Check if the given protocol version is older or as old as the other version
     * For example, these two will return true:
     *
     *     Protocol.MC_1_16_5.isBeforeInclusive(
     *              Protocol.MC_1_19
     *     );
     *
     *     Protocol.MC_1_14_2.isBeforeInclusive(
     *              Protocol.MC_1_14_2
     *     );
     *
     * These ones however will return false:
     *
     *     Protocol.MC_1_15_1.isBeforeInclusive(
     *              Protocol.MC_1_11_2
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is older or as old as the limit, false otherwise
    */
    public boolean isBeforeInclusive(Protocol otherVersion) {
        return this.version <= otherVersion.getVersion();
    }

    /**
     * <pre>
     * Check if the given protocol version is older than the other version
     * For example, these two will return true:
     *
     *     Protocol.MC_1_16_5.isBeforeInclusive(
     *              Protocol.MC_1_19
     *     );
     *
     * These ones however will return false:
     *
     *     Protocol.MC_1_14_2.isBeforeInclusive(
     *              Protocol.MC_1_14_2
     *     );
     *
     *     Protocol.MC_1_15_1.isBeforeInclusive(
     *              Protocol.MC_1_11_2
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is older than the limit, false otherwise
    */
    public boolean isBeforeExclusive(Protocol otherVersion) {
        return this.version < otherVersion.getVersion();
    }
}
