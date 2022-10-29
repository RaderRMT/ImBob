package fr.rader.imbob.windows.impl;

import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public class ProgressBarWindow extends AbstractWindow {

    private float progress;
    private String label;

    public ProgressBarWindow() {
        this.progress = 0;
        this.label = "Editing...";

        setWindowName("Please wait...");
        setVisible(false);
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
    }

    @Override
    protected void preRender() {
        ImGui.setNextWindowSize(279, 71);
        ImGui.setNextWindowPos(128, 152);
    }

    @Override
    protected void renderContent() {
        // we display a text for the progress bar.
        // this should be what is currently being done
        ImGui.text(label);
        // we change the progress bar color
        ImGui.pushStyleColor(ImGuiCol.PlotHistogram, 0x66FA9642); // imgui uses ABGR
        // and we draw the progress bar
        ImGui.progressBar(this.progress, -1, 0);
        // and we pop the color
        ImGui.popStyleColor();
    }

    /**
     * <pre>
     * Set the progress bar's fraction to another value.
     * The progress bar's fraction has to be between 0f and 1f,
     * where 0f means 0% and 1f means 100%.
     *
     * 25% would be 0.25f
     * 69% would be 0.69f
     * </pre>
     *
     * @param progress  The new progress bar's fraction
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }

    /**
     * Set the progress bar's label. This can be whatever you want
     * as long as it describes what is currently being done
     *
     * @param label     The new progress bar's label
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
