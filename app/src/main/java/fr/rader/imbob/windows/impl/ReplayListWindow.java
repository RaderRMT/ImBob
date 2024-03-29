package fr.rader.imbob.windows.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import fr.rader.imbob.tasks.TaskExecutor;
import fr.rader.imbob.utils.OS;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

public class ReplayListWindow extends AbstractWindow {

    private final ImInt selectedReplay;
    private final List<File> replays;

    private FileExplorerWindow fileExplorer;
    private TaskListWindow taskListWindow;
    private ProgressBarWindow progressBar;

    private boolean isReplayNameListDirty = false;
    private String[] replaysName;

    public ReplayListWindow() {
        this.selectedReplay = new ImInt();
        this.replays = new ArrayList<>();
        this.replaysName = new String[0];

        setWindowName("Replay List");
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
    }

    @Override
    public void init() {
        this.taskListWindow = getWindowManager().getWindowByClass(TaskListWindow.class);
        this.progressBar = getWindowManager().getWindowByClass(ProgressBarWindow.class);

        this.fileExplorer = getWindowManager().getWindowByClass(FileExplorerWindow.class);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        ImGui.setNextWindowSize(248, 125);
        ImGui.setNextWindowPos(0, menuBarHeight + 101);
    }

    @Override
    protected void renderContent() {
        // we create a button to add a replay.
        // all this does is showing the file explorer
        if (ImGui.button("Add Replay...")) {
            this.fileExplorer.open();
        }

        // we get the selected files from the file explorer
        List<File> selectedFiles = this.fileExplorer.getSelectedFiles();
        // if the list is null, this means no files were selected,
        // so we don't try and add nothing and we don't have to
        // update the replay name list
        if (selectedFiles != null) {
            // however, if the user selected at least one file,
            // we add them all to the replays list
            for (File selectedFile : selectedFiles) {
                // we only add the file if it's not
                // already contained in the replays list
                if (!this.replays.contains(selectedFile)) {
                    this.replays.add(selectedFile);
                }
            }

            // and we set this flag to true so we can update the replay name list
            this.isReplayNameListDirty = true;
        }
        
        ImGui.sameLine();

        ImGui.beginDisabled(
                this.replays.isEmpty() ||
                ( this.selectedReplay.get() < 0 ||
                  this.selectedReplay.get() >= this.replays.size() )
        );
        // we create a button to remove the selected replay
        if (ImGui.button("Remove Selected")) {
            this.replays.remove(this.selectedReplay.get());
            this.isReplayNameListDirty = true;
        }

        ImGui.endDisabled();

        // if we set the replay name list dirty flag to true,
        // we update the name list ad they'll be
        // printed on the screen soon after
        if (this.isReplayNameListDirty) {
            updateReplayNameList();
        }

        ImGui.pushItemWidth(150);
        // we show the list of replay names
        ImGui.listBox("Replays", this.selectedReplay, this.replaysName, 2);
        ImGui.popItemWidth();

        ImGui.beginDisabled(
                this.replays.isEmpty() ||
                ( this.selectedReplay.get() < 0 ||
                  this.selectedReplay.get() >= this.replays.size() ) ||
                this.taskListWindow.getTasks().isEmpty()
        );
        // if we click on the Edit Selected button and
        // have at least one replay in the replay list
        if (ImGui.button("Edit Selected")) {
            // then we move the selected replay to
            // the imbob folder and we edit it
            moveAndEditReplays(this.replays.get(this.selectedReplay.get()));
        }

        ImGui.endDisabled();

        ImGui.sameLine();

        ImGui.beginDisabled(this.replays.isEmpty() || this.taskListWindow.getTasks().isEmpty());
        // if we click on the Edit All button and
        // have at least one replay in the replay list
        if (ImGui.button("Edit All") && !this.replays.isEmpty()) {
            // then we move the replays to
            // the imbob folder and we edit them
            moveAndEditReplays(this.replays.toArray(new File[0]));
        }

        ImGui.endDisabled();
    }

    /**
     * Move the replays given as the first parameter to
     * the {@link OS#getImBobFolder()} folder and edit them.
     * This will apply all the tasks contained in the {@link TaskListWindow}'s task list
     *
     * @param replays   The list of replays to edit.
     */
    private void moveAndEditReplays(File... replays) {
        // we create a list of file, this will be the replays
        // to edit in the imbob folder
        List<File> replaysToEdit = new ArrayList<>();

        // we loop through each replay so we can move them
        for (File replay : replays) {
            // we create the file in the imbob folder
            File replayCopy = new File(OS.getImBobFolder() + replay.getName());

            try {
                // we copy the replay file to the imbob folder
                Files.copy(
                        replay.toPath(),
                        replayCopy.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                // and we add the file from the imbob folder to
                // the replaysToEdit list so we can edit those and not the original replays
                replaysToEdit.add(replayCopy);
            } catch (IOException e) {
                LoggerWindow.error(e.getMessage());
            }
        }

        // we then create a new instance of the task executor,
        // this will take the tasks to execute, the replays to edit
        // and the progress bar as the constructor's parameter
        TaskExecutor executor = new TaskExecutor(this.taskListWindow.getTasks(), replaysToEdit, this.progressBar);
        // finally, we can apply all the task edits to the replays
        executor.applyAllTaskEdits();
    }

    /**
     * Update the internal replaysName list with the replay names from the replays list
     */
    private void updateReplayNameList() {
        // we create a new list of string with the size of the replays list.
        // each element will be the name of the corresponding replay at the same index
        this.replaysName = new String[this.replays.size()];
        // we loop through each element in the replays list
        for (int i = 0; i < this.replays.size(); i++) {
            // and we set the replay name
            this.replaysName[i] = this.replays.get(i).getName();
        }
    }
}
