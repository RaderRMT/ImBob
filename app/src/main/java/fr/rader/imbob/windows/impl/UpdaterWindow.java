package fr.rader.imbob.windows.impl;

import fr.rader.imbob.updater.PSLUpdater;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class UpdaterWindow extends AbstractWindow {

    private PSLUpdater updater;

    public UpdaterWindow() {
        setVisible(false);
        setWindowName("Updater");
        setCloseable(true);
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        ImGui.setNextWindowSize(535, 375);
        ImGui.setNextWindowPos(0, menuBarHeight);
    }

    @Override
    protected void renderContent() {
        ImGui.text("PSL files have been updated to version " + this.updater.getGithubVersion());
        ImGui.text("Changelog: " + this.updater.getGithubUpdateMessage());
    }

    public void setUpdater(PSLUpdater updater) {
        this.updater = updater;
    }
}
