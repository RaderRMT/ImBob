package fr.rader.imbob.protocol;

public enum ProtocolVersion {

    MC_1_8(47),
    MC_1_8_9(47),

    MC_1_9(107),
    MC_1_9_1(108),
    MC_1_9_2(109),
    MC_1_9_3(110),
    MC_1_9_4(110),

    MC_1_10(210),

    MC_1_11(315),
    MC_1_11_1(316),
    MC_1_11_2(316),

    MC_1_12(335),
    MC_1_12_1(338),
    MC_1_12_2(340),

    MC_1_14(477),
    MC_1_14_1(480),
    MC_1_14_2(485),
    MC_1_14_3(490),
    MC_1_14_4(498),

    MC_1_15(573),
    MC_1_15_1(575),
    MC_1_15_2(578),

    MC_1_16(735),
    MC_1_16_1(736),
    MC_1_16_2(751),
    MC_1_16_3(753),
    MC_1_16_4(754),
    MC_1_16_5(754),

    MC_1_17(755),
    MC_1_17_1(756),

    MC_1_18(757),
    MC_1_18_1(757),
    MC_1_18_2(758),

    MC_1_19(759),
    MC_1_19_1(760),
    MC_1_19_2(760);

    private final int protocolId;

    private ProtocolVersion(int protocolId) {
        this.protocolId = protocolId;
    }

    /**
     * <pre>
     * Check if the given protocol version is newer or as new as the other version
     * For example, these two will return true:
     *
     *     ProtocolVersion.MC_1_19.isAfterInclusive(
     *              ProtocolVersion.MC_1_16_5
     *     );
     *
     *     ProtocolVersion.MC_1_14_2.isAfterInclusive(
     *              ProtocolVersion.MC_1_14_2
     *     );
     *
     * This one however will return false:
     *
     *     ProtocolVersion.MC_1_11_2.isAfterInclusive(
     *              ProtocolVersion.MC_1_15_1
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is newer or as old as the other version, false otherwise
    */
    public boolean isAfterInclusive(ProtocolVersion otherVersion) {
        return this.protocolId >= otherVersion.protocolId;
    }

    /**
     * <pre>
     * Check if the given protocol version is newer than the other version
     * For example, these two will return true:
     *
     *     ProtocolVersion.MC_1_19.isAfterExclusive(
     *              ProtocolVersion.MC_1_16_5
     *     );
     *
     * These ones however will return false:
     *
     *     ProtocolVersion.MC_1_14_2.isAfterExclusive(
     *              ProtocolVersion.MC_1_14_2
     *     );
     *
     *     ProtocolVersion.MC_1_11_2.isAfterExclusive(
     *              ProtocolVersion.MC_1_15_1
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is newer than the other version, false otherwise
    */
    public boolean isAfterExclusive(ProtocolVersion otherVersion) {
        return this.protocolId > otherVersion.protocolId;
    }

    /**
     * <pre>
     * Check if the given protocol version is older or as old as the other version
     * For example, these two will return true:
     *
     *     ProtocolVersion.MC_1_16_5.isBeforeInclusive(
     *              ProtocolVersion.MC_1_19
     *     );
     *
     *     ProtocolVersion.MC_1_14_2.isBeforeInclusive(
     *              ProtocolVersion.MC_1_14_2
     *     );
     *
     * These ones however will return false:
     *
     *     ProtocolVersion.MC_1_15_1.isBeforeInclusive(
     *              ProtocolVersion.MC_1_11_2
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is older or as old as the limit, false otherwise
    */
    public boolean isBeforeInclusive(ProtocolVersion otherVersion) {
        return this.protocolId <= otherVersion.protocolId;
    }

    /**
     * <pre>
     * Check if the given protocol version is older than the other version
     * For example, these two will return true:
     *
     *     ProtocolVersion.MC_1_16_5.isBeforeInclusive(
     *              ProtocolVersion.MC_1_19
     *     );
     *
     * These ones however will return false:
     *
     *     ProtocolVersion.MC_1_14_2.isBeforeInclusive(
     *              ProtocolVersion.MC_1_14_2
     *     );
     *
     *     ProtocolVersion.MC_1_15_1.isBeforeInclusive(
     *              ProtocolVersion.MC_1_11_2
     *     );
     * </pre>
     *
     * @param otherVersion Protocol version to check against
     * @return True if the protocol version is older than the limit, false otherwise
    */
    public boolean isBeforeExclusive(ProtocolVersion otherVersion) {
        return this.protocolId < otherVersion.protocolId;
    }

    /**
     * <pre>
     * Return a ProtocolVersion enum entry for the given protocol id
     *
     * Example:
     *
     *     ProtocolVersion protocolVersion = ProtocolVersion.getProtocolFromId(754);
     *     System.out.println(protocolVersion.protocolId);   // 754
     * </pre>
     *
     * @param protocolId The protocol id to get the ProtocolVersion from
     * @return           The corresponding ProtocolVersion if the version is supported, {@code null} otherwise.
    */
    public static ProtocolVersion getProtocolFromId(int protocolId) {
        // we loop through each protocol version enum entry
        for (ProtocolVersion version : values()) {
            // if the protocol version has the same
            // protocol id as the one given as a parameter,
            // we return the protocolVersion
            if (version.protocolId == protocolId) {
                return version;
            }
        }

        // if no valid ProtocolVersion has been found,
        // we return null and let the user handle the error
        return null;
    }

    public int getProtocolId() {
        return this.protocolId;
    }
}

