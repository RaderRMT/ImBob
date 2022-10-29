package fr.rader.imbob.windows.impl;

import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class LicensesWindow extends AbstractWindow {

    public LicensesWindow() {
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
        setVisible(false);
        setWindowName("Licenses");
        setCloseable(true);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        ImGui.setNextWindowSize(535, 375);
        ImGui.setNextWindowPos(0, menuBarHeight);
    }
    
    @Override
    protected void renderContent() {
        ImGui.text("This program uses the following open source software:");
        ImGui.newLine();
        ImGui.text("Gson (Apache 2.0)");
        ImGui.text("imgui-java (Apache 2.0)");
    }
}
