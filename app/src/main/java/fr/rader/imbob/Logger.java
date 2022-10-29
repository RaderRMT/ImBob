package fr.rader.imbob;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.rader.imbob.utils.DateUtils;
import fr.rader.imbob.utils.OS;
import fr.rader.imbob.utils.io.FileUtils;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

public class Logger {

    /** DateTime patterns */
    private static final String DATETIME_LOG_PATTERN = "[yyyy-MM-dd] [HH:mm:ss]";
    private static final String DATETIME_FILE_PATTERN = "yyyy-MM-dd_HH-mm-ss";

    /** Path to the main logs folder */
    private static final String LOG_PATH = OS.getBobLiteFolder() + "logs/";

    /** Path to the current log */
    private static final String CURRENT_LOG = LOG_PATH + DateUtils.getFormattedDate(DATETIME_FILE_PATTERN) + ".log";

    private static Logger instance;

    private static int firstColumnSpacing = 50;
    private static int secondColumnSpacing = 120;

    private final List<LogItem> logItems;

    private final ImString searchString;

    private FileWriter writer;

    /**
     * This constructs a new Logger.
     * It is package private because we don't
     * want to create a new instance of this class
    */
    Logger() {
        instance = this;

        this.logItems = new ArrayList<>();
        this.searchString = new ImString();

        File file = FileUtils.makeFile(CURRENT_LOG, true);
        if (file == null) {
            throw new IllegalStateException();
        }

        try {
            this.writer = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the logger's render method.<br>
     * It is package private because we only
     * need to call it in the main class' process method.
    */
    void render(float menuBarHeight) {
        ImGui.setNextWindowSize(535, 150);
        ImGui.setNextWindowPos(0, menuBarHeight + 225);

        // we create the logger window
        if (ImGui.begin("Logs", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove)) {
            // we add a clear button, so we
            // can clear the logs
            if (ImGui.button("Clear")) {
                this.logItems.clear();
            }

            ImGui.sameLine();
            // we add a 150px input field
            // so we can look for specific
            // things in the logs
            ImGui.pushItemWidth(150);
            ImGui.inputText("Search", this.searchString);
            ImGui.popItemWidth();

            ImGui.separator();
            // we create a new child, so we can
            // scroll only the log items
            ImGui.beginChild("ScrollingRegion");
            
            // set 3 columns:
            //   one for the verbosity
            //   one for the class name
            //   one for the log message
            ImGui.columns(3);

            // this is kinda a hack, we only want
            // to set the column width once.
            if (firstColumnSpacing > 0) {
                ImGui.setColumnWidth(0, firstColumnSpacing);
                firstColumnSpacing = 0;
            }

            // we do the same hack here
            if (secondColumnSpacing > 0) {
                ImGui.setColumnWidth(1, secondColumnSpacing);
                secondColumnSpacing = 0;
            }

            // we loop through each log items
            Iterator<LogItem> iterator = this.logItems.iterator();
            while (iterator.hasNext()) {
                LogItem item = iterator.next();

                // skip the item if it doesn't
                // contain what we're looking for
                if (!item.getMessage().toLowerCase().contains(this.searchString.get().toLowerCase())) {
                    continue;
                }

                // we get the color for each message
                ImVec4 color = item.getVerbosity().getColor();

                // we push the color
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, color.x, color.y, color.z, color.w);
                // we write the leve
                ImGui.textUnformatted(item.getVerbosity().getLevel());
                ImGui.nextColumn();
                // we write the class name
                ImGui.textUnformatted(item.getClassName());
                ImGui.nextColumn();
                // we write the message
                ImGui.textUnformatted(item.getMessage());
                ImGui.nextColumn();

                // we pop the color, unless we want
                // every text to have a different color
                ImGui.popStyleColor();
            }

            // we reset the columns count to 1
            ImGui.columns(1);
            // and scroll to the bottom if a new log entry
            // has been added, and if we're at the bottom
            // of the log scroller
            if (ImGui.getScrollY() >= ImGui.getScrollMaxY()) {
                ImGui.setScrollHereY(1);
            }

            // we end the scrolling region
            ImGui.endChild();
        }

        // we end the window, as ImGui is stack based
        ImGui.end();
    }

    /**
     * Send a red error message to the logger window
     * 
     * @param className The class that called the method
     * @param message   The log message
    */
    public void error(String message) {
        String callee = Thread.currentThread().getStackTrace()[2].getClassName();
        callee = callee.substring(callee.lastIndexOf('.') + 1);

        log(
                Verbosity.ERROR,
                callee,
                message
        );
    }

    /**
     * Send a yellow warn message to the logger window
     * 
     * @param className The class that called the method
     * @param message   The log message
    */
    public void warn(String message) {
        String callee = Thread.currentThread().getStackTrace()[2].getClassName();
        callee = callee.substring(callee.lastIndexOf('.') + 1);

        log(
                Verbosity.WARN,
                callee,
                message
        );
    }

    /**
     * Send a white normal message to the logger window
     * 
     * @param className The class that called the method
     * @param message   The log message
    */
    public void info(String message) {
        String callee = Thread.currentThread().getStackTrace()[2].getClassName();
        callee = callee.substring(callee.lastIndexOf('.') + 1);

        log(
                Verbosity.INFO,
                callee,
                message
        );
    }

    /**
     * This just creates a new log item and adds it to the log items list.
     * This method is private because there's no need having this public
     *
     * @param verbosity The log entry's verbosity (and color)
     * @param className The class that called the method
     * @param message   The log message
    */
    private void log(Verbosity verbosity, String className, String message) {
        if (this.writer == null) {
            return;
        }

        this.logItems.add(new LogItem(verbosity, className, message));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DateUtils.getFormattedDate(DATETIME_LOG_PATTERN));
        stringBuilder.append(" ");
        stringBuilder.append(verbosity.getLevel());
        stringBuilder.append(" from ");
        stringBuilder.append(className);
        stringBuilder.append(": ");
        stringBuilder.append(message);
        stringBuilder.append('\n');

        System.out.print(stringBuilder.toString());

        try {
            this.writer.append(stringBuilder.toString());
            this.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This just returns the logger's instance, so we only need one logger.
     *
     * @return The logger's instance
    */
    public static Logger getInstance() {
        return instance;
    }

    private static class Verbosity {

        public static final Verbosity ERROR = new Verbosity("Error", new ImVec4(1, 0, 0, 1));
        public static final Verbosity WARN = new Verbosity("Warn", new ImVec4(1, 1, 0, 1));
        public static final Verbosity INFO = new Verbosity("Info", new ImVec4(1, 1, 1, 1));

        private final String level;
        private final ImVec4 color;

        private Verbosity(String level, ImVec4 color) {
            this.level = level;
            this.color = color;
        }

        public String getLevel() {
            return this.level;
        }

        public ImVec4 getColor() {
            return this.color;
        }
    }

    private static class LogItem {

        private final Verbosity verbosity;
        private final String className;
        private final String message;

        LogItem(Verbosity verbosity, String className, String message) {
            this.verbosity = verbosity;
            this.className = className;
            this.message = message;
        }

        public Verbosity getVerbosity() {
            return verbosity;
        }

        public String getClassName() {
            return className;
        }

        public String getMessage() {
            return message;
        }
    }
}
