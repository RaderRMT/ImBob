package fr.rader.imbob.windows.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.rader.imbob.utils.OS;
import fr.rader.imbob.utils.StringUtils;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

public class FileExplorerWindow extends AbstractWindow {

    private static FileExplorerWindow instance;

    private final ImInt selectedDrive;
    private String[] drives;

    private String path;

    private boolean isDirty = true;
    private String[] fileNames;

    private String[] validExtensions;

    private final List<String> selectedFileNames;
    private final List<String> selectedFilePaths;

    private boolean canSelectMultipleFiles;

    public FileExplorerWindow() {
        this.selectedDrive = new ImInt(0);
        this.path = System.getProperty("user.home").replace('\\', '/') + '/';
        this.selectedFileNames = new ArrayList<>();
        this.selectedFilePaths = new ArrayList<>();
        this.canSelectMultipleFiles = false;

        instance = this;

        // we get all the available drives
        updateDrives();

        setVisible(false);
        setCloseable(true);
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
        setWindowName("File Explorer");
    }

    @Override
    protected void preRender() {
        ImGui.setNextWindowSize(535, 375);
        ImGui.setNextWindowPos(0, 0);
    }

    @Override
    protected void renderContent() {
        ImGui.pushItemWidth(75);
        // this is a dropdown menu where we can select a specific drive
        // (the drive has to be mounted on linux to be visible in this list!)
        if (ImGui.beginCombo("##select_drive", this.drives[this.selectedDrive.get()])) {
            // we loop through each drive we discovered
            for (int i = 0; i < this.drives.length; i++) {
                // we create an entry in the dropdown for the drive
                if (ImGui.selectable(this.drives[i])) {
                    // if we clicked on the button, we set
                    // the selected drive index to the drive index
                    this.selectedDrive.set(i);

                    // we set the path to the drive's path
                    this.path = this.drives[i];
                    // if we're on linux or macos, drives are mounted to "/media/username/drivename/"
                    if ((OS.isLinux() || OS.isMacOS()) && i != 0) {
                        this.path = "/media/" + System.getProperty("user.name") + '/' + this.path + '/';
                    }

                    // then we tell the file explorer to reload the files
                    this.isDirty = true;
                }
            }

            ImGui.endCombo();
        }

        ImGui.popItemWidth();

        // we build the path to the directory, where each
        // parent directory is a button to make navigation easier.
        // this string array will store each parent folder in the path
        String[] splittedPath = this.path.split("/");
        // we loop through each parent folder in the path
        for (int i = 0; i < splittedPath.length; i++) {
            // we keep each button on the same line
            ImGui.sameLine();
            // we create a button for the folder. if it is
            // the root directory (on linux), we set it to '/'
            if (ImGui.smallButton(splittedPath[i].isEmpty() ? "/" : splittedPath[i])) {
                // if we click the button, we set the path
                // to the folder we clicked the button of
                this.path = this.path.substring(
                        0,
                        StringUtils.indexOf(this.path, '/', i + 1)
                );

                // and we tell the file explorer to reload the files
                this.isDirty = true;
            }
        }

        // we reload all the files we see in the file
        // explorer if we decide to change directory
        if (this.isDirty) {
            reloadFiles();
            this.isDirty = false;
        }

        // we create a list that will hold every file in the current directory
        if (ImGui.beginListBox("##files_list", -1, -ImGui.getFrameHeightWithSpacing())) {
            boolean selected = false;
            for (String fileName : this.fileNames) {
                // this is the file name without its type prefix
                // ("[D]" for directories, "[F]" for files)
                String actualFileName = fileName.substring(4);
                // we look if the file we're looping over is the one we selected
                selected = this.selectedFileNames.contains(actualFileName);

                // we create a list element with the file name as its name
                if (ImGui.selectable(fileName, selected)) {
                    if (fileName.startsWith("[D]")) {
                        // if we click on the element and the file name starts
                        // with "[D]" (so if the file is a directory), then we
                        // add the file name without the type prefix
                        // to the path and we reload the list elements
                        this.path += actualFileName + '/';
                        this.isDirty = true;
                    } else {
                        if (!this.canSelectMultipleFiles) {
                            // if we can select multiple files, we clear both lists
                            // as they might contain the previous selected file
                            this.selectedFileNames.clear();
                            this.selectedFilePaths.clear();
                            // then we add the file we selected in both lists
                            this.selectedFilePaths.add(this.path + actualFileName);
                            this.selectedFileNames.add(actualFileName);
                        } else if (selected) {
                            // if the current item is selected and we click it,
                            // we unselect the file by removing it from the two following lists
                            this.selectedFilePaths.remove(this.path + actualFileName);
                            this.selectedFileNames.remove(actualFileName);
                        } else {
                            // otherwise, we add the selected file path and name to their respective list
                            // so we can get the files if the file explorer has been closed
                            this.selectedFilePaths.add(this.path + actualFileName);
                            this.selectedFileNames.add(actualFileName);
                        }
                    }
                }
            }

            ImGui.endListBox();
        }

        // if we click on the Ok button, we just hide the window
        if (ImGui.button("Ok")) {
            setVisible(false);
        }

        ImGui.sameLine();
        // if we click on the cancel button, we reset
        // the selected file name and path and hide the window
        if (ImGui.button("Cancel")) {
            this.selectedFileNames.clear();
            this.selectedFilePaths.clear();
            setVisible(false);
        }
    }

    /**
     * Update the available drive list.
     *
     * On windows, all drives will be shown by default.
     * On linux and macos, drives will have to mounted before they can be visible.
     */
    public void updateDrives() {
        if (OS.isWindows()) {
            // on windows, we can get all root files with the File#listRoots() method
            File[] windowsDrives = File.listRoots();

            // we create a new list for the drives
            this.drives = new String[windowsDrives.length];
            // then we loop for the entire size of the drives list
            for (int i = 0; i < windowsDrives.length; i++) {
                // and for each drive in the windowsDrives File list,
                // we get the path (with forward slashes) and we add it to the drives list
                this.drives[i] = windowsDrives[i].getAbsolutePath().replace('\\', '/');
            }
        } else {
            // on Linux and MacOS, you have to mount the drive
            // before it can be visible to the app.
            // mounted drives are in "/media/username/"
            String[] linuxDrives = new File("/media/" + System.getProperty("user.name")).list();

            // we create a new list for the drives
            this.drives = new String[linuxDrives.length + 1];
            // we set the first drive to "/" as that's the
            // root directory and will always be there
            this.drives[0] = "/";
            // then we loop for the entire size of the drives list
            for (int i = 0; i < linuxDrives.length; i++) {
                // and for each drive in the linuxDrives list,
                // we add the path to the drives list
                this.drives[i + 1] = linuxDrives[i];
            }
        }
    }

    /**
     * Reload all files in the file explorer
     */
    private void reloadFiles() {
        // we clear the selected file name and path lists, as we changed directory
        this.selectedFileNames.clear();
        this.selectedFilePaths.clear();

        // we get the filtered files from the path
        File[] files = new File(this.path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // we keep the file if it is a directory
                if (file.isDirectory()) {
                    return true;
                }

                // we keep the file if no specific extension is required
                if (validExtensions == null) {
                    return true;
                }

                // we loop through each extension
                for (String extension : validExtensions) {
                    // if the file ends with the extension, we keep it
                    if (file.getName().endsWith(extension)) {
                        return true;
                    }
                }

                // otherwise, we ignore the file
                return false;
            }
        });

        // we create a new list for our file names
        this.fileNames = new String[files.length];

        // we loop until we reach the end of the file name list
        for (int i = 0; i < files.length; i++) {
            // we get the file
            File file = files[i];

            // and we prepend it with "[D]" or "[F]" depending on if it's a file or a directory
            this.fileNames[i] = (file.isFile() ? "[F] " : "[D] ") + file.getName();
        }

        // finally, we sort the file name list alphabetically
        Arrays.sort(this.fileNames);
    }

    @Override
    public void setVisible(boolean visibility) {
        // if we show the file explorer
        if (visibility) {
            // we update the available drives
            updateDrives();
            // we update the files in the file explorer
            this.isDirty = true;
            // and we reset the selected file name 
            // and path lists so no files are selected
            this.selectedFileNames.clear();
            this.selectedFilePaths.clear();
        }

        // if we're selecting a drive that has been removed
        // and we're now out of bounds, we set it to the last drive index
        if (this.selectedDrive.get() >= this.drives.length) {
            this.selectedDrive.set(this.drives.length - 1);
        }

        // finally, we change the window's visibility
        super.setVisible(visibility);
    }

    public static FileExplorerWindow getInstance() {
        return instance;
    }

    /**
     * Returns the selected file in the file explorer
     *
     * @return  The selected file, or null if no file was selected
     */
    public List<File> getSelectedFiles() {
        // if the window is still visible, we return null
        if (isVisible()) {
            return null;
        }

        // we create a list of files that will be returned if it's not empty
        List<File> files = new ArrayList<>();
        // we loop through each file path in the selectedFilePaths list
        for (String filePath : this.selectedFilePaths) {
            // we make a file out of it
            File file = new File(filePath);
            // and if the file exist and it is a file and
            // not a directory, then we add it to the files list
            if (file.exists() && file.isFile()) {
                files.add(file);
            }
        }

        // if the file exists, we clear the selected file name and path lists
        this.selectedFileNames.clear();
        this.selectedFilePaths.clear();

        // and we return the selected files
        return files.isEmpty() ? null : files;
    }

    /**
     * Set the supported file extensions. The file explorer will
     * only look for files with one of the given extensions
     *
     * @param extensions        The supported file extensions
     */
    public void setExtensions(String[] extensions) {
        this.validExtensions = extensions;
    }

    /**
     * Setting this to true will allow the user to select multiple files
     *
     * @param canSelectMultipleFiles    true to select multiple files,
     *                                  false to select one file
     */
    public void setMultipleFileSelect(boolean canSelectMultipleFiles) {
        this.canSelectMultipleFiles = canSelectMultipleFiles;
    }
}
