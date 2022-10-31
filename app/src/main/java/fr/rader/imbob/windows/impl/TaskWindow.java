package fr.rader.imbob.windows.impl;

import java.util.Arrays;
import java.util.List;

import fr.rader.imbob.tasks.AbstractTask;
import fr.rader.imbob.tasks.impl.TimeChangerTask;
import fr.rader.imbob.tasks.impl.WeatherChangerTask;
import fr.rader.imbob.windows.AbstractWindow;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

public class TaskWindow extends AbstractWindow {

    private final ImInt taskInput;

    private TaskListWindow taskListWindow;

    private static final List<AbstractTask> TASKS = Arrays.asList(
            new TimeChangerTask(),
            new WeatherChangerTask()
    );

    public TaskWindow() {
        this.taskInput = new ImInt(0);

        setWindowName("Tasks");
        setWindowFlags(ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);
    }

    @Override
    public void init() {
        this.taskListWindow = getWindowManager().getWindowByClass(TaskListWindow.class);
    }

    @Override
    protected void preRender(float menuBarHeight) {
        ImGui.setNextWindowSize(288, 226);
        ImGui.setNextWindowPos(247, menuBarHeight);
    }

    @Override
    protected void renderContent() {
        ImGui.pushItemWidth(150);

        // we create a combo, this is the task list
        if (ImGui.beginCombo("##task_list_dropdown", TASKS.get(taskInput.get()).getTaskName())) {
            for (int i = 0; i < TASKS.size(); i++) {
                // if we selected a new task, we set the taskInput value
                // to the index of the task we selected
                if (ImGui.selectable(TASKS.get(i).getTaskName())) {
                    this.taskInput.set(i);
                }
            }

            ImGui.endCombo();
        }

        ImGui.sameLine();
        
        // we get the selected task
        AbstractTask selectedTask = TASKS.get(taskInput.get());
        String buttonContent = "Add Task";
        if (this.taskListWindow.hasTask(selectedTask)) {
            buttonContent = "Update Task";
        }

        // we add a button to add/update a task.
        // updating a task is basically adding the task again
        if (ImGui.button(buttonContent)) {
            this.taskListWindow.addTask(selectedTask);
        }

        // and we render the task's content
        selectedTask.render();
    }
}
