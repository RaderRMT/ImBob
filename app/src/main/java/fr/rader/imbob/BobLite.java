package fr.rader.imbob;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import fr.rader.imbob.windows.AbstractWindow;
import fr.rader.imbob.windows.impl.FileExplorerWindow;
import fr.rader.imbob.windows.impl.ProgressBarWindow;
import fr.rader.imbob.windows.impl.ReplayListWindow;
import fr.rader.imbob.windows.impl.TaskListWindow;
import fr.rader.imbob.windows.impl.TaskWindow;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;

public class BobLite extends Application {

    private final Logger logger;
    private final List<AbstractWindow> windows;

    BobLite() {
        this.logger = new Logger();
        this.windows = new ArrayList<>();

        ProgressBarWindow progressBar = new ProgressBarWindow();
        TaskListWindow taskListWindow = new TaskListWindow();
        TaskWindow taskWindow = new TaskWindow();

        taskWindow.setTaskListWindowReference(taskListWindow);

        this.windows.add(progressBar);
        this.windows.add(new FileExplorerWindow());
        this.windows.add(taskWindow);
        this.windows.add(new ReplayListWindow(taskListWindow, progressBar));
        this.windows.add(taskListWindow);
    }

    @Override
    protected void initWindow(Configuration config) {
        // we change the window title
        config.setTitle("Bob Lite");
        // we set the window size
        config.setWidth(535);
        config.setHeight(375);

        // and we initialize the window
        super.initWindow(config);

        // we disable the window resize
        GLFW.glfwSetWindowAttrib(
                getHandle(),
                GLFW.GLFW_RESIZABLE,
                GLFW.GLFW_FALSE
        );
    }

    @Override
    protected void initImGui(Configuration config) {
        super.initImGui(config);

        // disable the imgui.ini file
        ImGui.getIO().setIniFilename(null);
    }

    @Override
    public void process() {
        // we loop through all windows and render them
        for (AbstractWindow window : this.windows) {
            window.render();
        }

        // and finally, we render the logger
        logger.render();
    }

    void start() {
        launch(this);
    }
}
