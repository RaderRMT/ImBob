package fr.rader.imbob;

import org.lwjgl.glfw.GLFW;

import fr.rader.imbob.windows.WindowManager;
import fr.rader.imbob.windows.impl.LicensesWindow;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;

public class BobLite extends Application {

    private static final int WINDOW_WIDTH = 535;
    private static final int WINDOW_HEIGHT = 375;

//    private final Logger logger;
    //private final List<AbstractWindow> windows;

    //private final LicensesWindow licensesWindow;

    private boolean isWindowResized = false;

    private final WindowManager windowManager;

    BobLite() {
//        this.logger = new Logger();
        //this.windows = new ArrayList<>();
        this.windowManager = new WindowManager();
        this.windowManager.initializeAllWindows();

        /*ProgressBarWindow progressBar = new ProgressBarWindow();
        TaskListWindow taskListWindow = new TaskListWindow();
        TaskWindow taskWindow = new TaskWindow();
        this.licensesWindow = new LicensesWindow();

        taskWindow.setTaskListWindowReference(taskListWindow);

        this.windows.add(progressBar);
        this.windows.add(new FileExplorerWindow());
        this.windows.add(taskWindow);
        this.windows.add(new ReplayListWindow(taskListWindow, progressBar));
        this.windows.add(taskListWindow);
        this.windows.add(licensesWindow);*/
    }

    @Override
    protected void initWindow(Configuration config) {
        // we change the window title
        config.setTitle("ImBob");
        // we set the window size
        config.setWidth(WINDOW_WIDTH);
        config.setHeight(WINDOW_HEIGHT);

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
        ImGui.beginMainMenuBar();
        float menuBarHeight = ImGui.getWindowSizeY();

        if (!this.isWindowResized) {
            GLFW.glfwSetWindowSize(getHandle(), WINDOW_WIDTH, WINDOW_HEIGHT + (int) menuBarHeight);
            this.isWindowResized = true;
        }

        if (ImGui.beginMenu("Menu")) {
            if (ImGui.menuItem("Licenses")) {
                this.windowManager.getWindowByClass(LicensesWindow.class).setVisible(true);
                //this.licensesWindow.setVisible(true);
            }

            ImGui.endMenu();
        }

        ImGui.endMainMenuBar();

        this.windowManager.renderAll(menuBarHeight);

        // and finally, we render the logger
//        logger.render(menuBarHeight);
    }

    void start() {
        launch(this);
    }
}
