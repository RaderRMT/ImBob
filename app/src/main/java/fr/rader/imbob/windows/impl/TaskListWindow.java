package fr.rader.imbob.windows.impl;

import java.util.ArrayList;
import java.util.List;

import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

public class TaskListWindow extends AbstractWindow {

    private final ImInt selectedTask = new ImInt();

    private final List<AbstractTask> tasks;

    private String[] taskNames;

    public TaskListWindow() {
        this.tasks = new ArrayList<>();
        this.taskNames = new String[0];

        setWindowName("Task List");
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        ImGui.setNextWindowSize(248, 102);
        ImGui.setNextWindowPos(0, menuBarHeight);
    }

    @Override
    protected void renderContent() {
        ImGui.pushItemWidth(150);
        // we have a list of tasks so the user knows what tasks will
        // be executed. this also allows the user to remove a task that they added
        ImGui.listBox("Tasks", this.selectedTask, this.taskNames, 2);

        ImGui.beginDisabled(this.tasks.isEmpty());
        // we add a Remove Task button
        if (ImGui.button("Remove Task")) {
            // if the user click the button, we'll check
            // if the user selected a task to remove
            if (!this.tasks.isEmpty() && this.selectedTask.get() < this.tasks.size()) {
                // if that's the case, we remove the task
                this.tasks.remove(this.selectedTask.get());
                // and update the task names for the list box
                updateNames();
            }
        }

        ImGui.endDisabled();
    }

    /**
     * Add a task to the task list.
     * If the task list already contains the task, it'll be removed
     * so the new one can be added, acting as an update.
     *
     * @param task      The task to add
     */
    public void addTask(AbstractTask task) {
        // we remove the task if the task list contains
        // a task with the same name as the one
        // we're adding, so we can update a task
        this.tasks.removeIf(taskFromList -> taskFromList.getTaskName().equals(task.getTaskName()));
        // we then add the task
        this.tasks.add(task);
        // and finally we update the task names for the list box
        updateNames();
    }

    /**
     * Checks if the task list contains a task
     *
     * @param task      The task to look for
     * @return          true if the task is in the list,
     *                  false otherwise
     */
    public boolean hasTask(AbstractTask task) {
        // we loop through each task in the list
        for (AbstractTask listTask : this.tasks) {
            // if the tasks list has the task we're looking for,
            // we return true
            if (listTask.getTaskName().equals(task.getTaskName())) {
                return true;
            }
        }

        // if the list doesn't contain the task,
        // we return false
        return false;
    }

    /**
     * Update the internal list of task names
     * used by the task name list box
     */
    private void updateNames() {
        // we create a new list of string with the size of the tasks list.
        // each element will be the name of the corresponding task at the same index
        this.taskNames = new String[this.tasks.size()];
        // we loop through each element in the tasks list
        for (int i = 0; i < this.tasks.size(); i++) {
            // and we set the task name
            this.taskNames[i] = this.tasks.get(i).getTaskName();
        }
    }

    /**
     * Returns the list of all the tasks added so far
     * 
     * @return  The list of all the tasks this window contains
     */
    public List<AbstractTask> getTasks() {
        return this.tasks;
    }
}
