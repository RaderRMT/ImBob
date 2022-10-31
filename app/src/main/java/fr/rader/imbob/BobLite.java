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

    private boolean isWindowResized = false;

    private final WindowManager windowManager;

    BobLite() {
        this.windowManager = new WindowManager();
        this.windowManager.initializeAllWindows();
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
            }

            ImGui.endMenu();
        }

        ImGui.endMainMenuBar();

        // we render all the windows
        this.windowManager.renderAll(menuBarHeight);
    }

    void start() {
        launch(this);
    }
}
