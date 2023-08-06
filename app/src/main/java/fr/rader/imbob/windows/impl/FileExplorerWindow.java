package fr.rader.imbob.windows.impl;

import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiWindowFlags;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileExplorerWindow extends AbstractWindow {

    private static final int WINDOW_FLAGS = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove;
    private static final int FILE_DIALOG_FLAGS = ImGuiFileDialogFlags.HideColumnDate;

    private Map<String, String> selectedFiles;
    private float menuBarHeight;

    public FileExplorerWindow() {
        setVisible(false);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        this.menuBarHeight = menuBarHeight;
    }

    @Override
    protected void renderContent() {
        ImGui.setNextWindowPos(0, 0);

        if (ImGuiFileDialog.display("filechooser", WINDOW_FLAGS, 535, 375 + this.menuBarHeight, 535, 375 + this.menuBarHeight)) {
            if (ImGuiFileDialog.isOk()) {
                this.selectedFiles = ImGuiFileDialog.getSelection();
            }

            ImGuiFileDialog.close();
            setVisible(false);
        }
    }

    public List<File> getSelectedFiles() {
        Map<String, String> out = this.selectedFiles;
        this.selectedFiles = null;

        if (out == null) {
            return null;
        }

        return out.values().stream().map(File::new).collect(Collectors.toList());
    }

    public void open() {
        ImGuiFileDialog.openModal("filechooser", "Choose Replay", ".mcpr", ".", 1, 42, FILE_DIALOG_FLAGS);

        setVisible(true);
    }
}
