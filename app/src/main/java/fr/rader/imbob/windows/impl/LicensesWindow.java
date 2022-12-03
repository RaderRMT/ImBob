package fr.rader.imbob.windows.impl;

import java.util.ArrayList;
import java.util.List;

import fr.rader.imbob.utils.Pair;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public class LicensesWindow extends AbstractWindow {

    private final List<Pair<String, License>> dependencies;

    private int columnSpacing;

    public LicensesWindow() {
        this.dependencies = new ArrayList<>();
        this.dependencies.add(new Pair<String, License>("gson", License.APACHE_2));
        this.dependencies.add(new Pair<String, License>("imgui-java", License.APACHE_2));
        this.dependencies.add(new Pair<String, License>("commons-io", License.APACHE_2));
        this.dependencies.add(new Pair<String, License>("jgit", License.EDL_1));

        this.columnSpacing = 90;

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

        ImGui.columns(3);

        if (this.columnSpacing != 0) {
            ImGui.setColumnWidth(0, this.columnSpacing);
            ImGui.setColumnWidth(1, this.columnSpacing);
            this.columnSpacing = 0;
        }

        for (Pair<String, License> dependency : this.dependencies) {
            ImGui.textUnformatted(dependency.getKey());

            ImGui.nextColumn();
            ImGui.textUnformatted(dependency.getValue().getName());

            ImGui.nextColumn();
            ImGui.textUnformatted(dependency.getValue().getLink());

            ImGui.nextColumn();
        }
    }

    private static enum License {

        APACHE_2("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0.html"),
        EDL_1("EDL 1.0", "https://www.eclipse.org/org/documents/edl-v10.php");

        private final String name;
        private final String link;

        private License(String name, String link) {
            this.name = name;
            this.link = link;
        }

        public String getName() {
            return this.name;
        }

        public String getLink() {
            return this.link;
        }
    }
}
