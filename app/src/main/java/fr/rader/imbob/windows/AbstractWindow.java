package fr.rader.imbob.windows;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;

public abstract class AbstractWindow {

    /** 
     * This is the window name.<br>
     * The default value is "Unnamed window".
     */
    private String windowName;
    /**
     * This is the window flags.
     * The default value is {@link ImGuiWindowFlags#None}.
     */
    private int windowFlags;

    /**
     * This is the closeable "flag".
     * If this is set to true, a X will
     * show so we can close the window
     */
    private boolean closeable = false;

    /**
     * This is given to the {@link ImGui#begin(String, ImBoolean, int)} method
     * if the closeable "flag" is set to true
     */
    private final ImBoolean windowOpen;

    /**
     * This constructor is protected as we
     * don't want to create a new instance of this
     * class without having it begin extended by
     * another class.<br>
     * This constructor will just set the window's
     * default name and default flags.
     */
    protected AbstractWindow() {
        // we're setting the default name and window flags,
        // so even if we don't change them the program won't
        // crash because they were not defined
        this.windowName = "Unnamed window";
        this.windowFlags = ImGuiWindowFlags.None;

        // by default, the window is open
        this.windowOpen = new ImBoolean(true);
    }

    /**
     * This method is called at the start of the {@link AbstractWindow#render()} method,
     * and it is only called if the window is open.
     */
    protected void preRender(float menuBarHeight) {
    }

    /**
     * Render the window's content on the screen.<br>
     * This method is used to render everything
     * in the window more easily by abstracting the
     * window creation code and only letting the user
     * define what the window contains.
     */
    protected abstract void renderContent();

    /**
     * This method is called at the end of the {@link AbstractWindow#render()} method,
     * after the window has been ended.
     */
    protected void postRender() {
    }

    /**
     * This is the window render method. It begins an ImGui window
     * with the {@link AbstractWindow#windowName} and {@link AbstractWindow#windowFlags} values and
     * calls the {@link AbstractWindow#renderContent} method to draw the window's content.<br>
     * It is called every frame.<br>
     * If no name has been given, the default "Unnamed window" name is used.<br>
     * If no window flags have been given, the default {@link ImGuiWindowFlags#None} flag is used.
     */
    public final void render(float menuBarHeight) {
        // we ignore the render if the window is closed
        if (!this.windowOpen.get()) {
            return;
        }

        preRender(menuBarHeight);

        // if we can close the window, we begin
        // a window with the windowClose boolean,
        // otherwise we don't give it the windowClose boolean
        if (this.closeable) {
            ImGui.begin(this.windowName, this.windowOpen, this.windowFlags);
        } else {
            ImGui.begin(this.windowName, this.windowFlags);
        }

        // we render the content
        renderContent();

        // and we end the window
        ImGui.end();

        postRender();
    }

    public String getWindowName() {
        return this.windowName;
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }

    public int getWindowFlags() {
        return this.windowFlags;
    }

    public void setWindowFlags(int windowFlags) {
        this.windowFlags = windowFlags;
    }

    /**
     * Set the window to be closeable or not.
     *
     * @param closeable If closeable is true, the window can be closed.<br>
     *                  If closeable is false, the window can't be closed.
     */
    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    /**
     * Show or hide the window depending on the visibility.
     *
     * @param visibility If visibility is true, the window is open.<br>
     *                   If visibility is false, the window is closed.
     */
    public void setVisible(boolean visibility) {
        this.windowOpen.set(visibility);
    }

    public boolean isVisible() {
        return this.windowOpen.get();
    }
}
