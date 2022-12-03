package fr.rader.imbob.utils;

public class OS {

    private static final int PLATFORM;

    private static final int LINUX = 1;
    private static final int MACOSX = 2;
    private static final int WINDOWS = 3;

    public static String getImBobFolder() {
        return System.getProperty("user.home").replace("\\", "/") + "/.imbob/";
    }

    public static String getAssetsFolder() {
        return getImBobFolder() + "assets/";
    }

    public static boolean isWindows() {
        return PLATFORM == WINDOWS;
    }

    public static boolean isLinux() {
        return PLATFORM == LINUX;
    }

    public static boolean isMacOS() {
        return PLATFORM == MACOSX;
    }

    static {
        String osName = System.getProperty("os.name");

        if (osName.startsWith("Windows")) {
            PLATFORM = WINDOWS;
        } else if (!osName.startsWith("Linux")
                && !osName.startsWith("FreeBSD")
                && !osName.startsWith("SunOS")
                && !osName.startsWith("Unix")) {
            if (!osName.startsWith("Mac OS X") && !osName.startsWith("Darwin")) {
                throw new IllegalStateException("Unknown platform: " + osName);
            }

            PLATFORM = MACOSX;
        } else {
            PLATFORM = LINUX;
        }
    }
}
