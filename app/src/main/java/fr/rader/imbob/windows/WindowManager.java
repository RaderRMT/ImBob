package fr.rader.imbob.windows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.rader.imbob.windows.impl.FileExplorerWindow;
import fr.rader.imbob.windows.impl.LicensesWindow;
import fr.rader.imbob.windows.impl.LoggerWindow;
import fr.rader.imbob.windows.impl.ProgressBarWindow;
import fr.rader.imbob.windows.impl.ReplayListWindow;
import fr.rader.imbob.windows.impl.TaskListWindow;
import fr.rader.imbob.windows.impl.TaskWindow;

public class WindowManager {

    private static final List<Class<? extends AbstractWindow>> WINDOWS = Arrays.asList(
            ProgressBarWindow.class,
            FileExplorerWindow.class,
            TaskWindow.class,
            ReplayListWindow.class,
            TaskListWindow.class,
            LicensesWindow.class,
            LoggerWindow.class
    );

    private final List<AbstractWindow> windows;

    public WindowManager() {
        this.windows = new ArrayList<>();
    }

    public void initializeAllWindows() {
        for (Class<?> window : WINDOWS) {
            try {
                Constructor<?> constructor = window.getConstructor();

                this.windows.add((AbstractWindow) constructor.newInstance());
            } catch (
                    NoSuchMethodException 
                    | SecurityException 
                    | InstantiationException 
                    | IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e
            ) {
                e.printStackTrace();
            }
        }

        for (AbstractWindow window : this.windows) {
            window.setWindowManager(this);
            window.init();
        }
    }

    public <T extends AbstractWindow> T getWindowByClass(Class<T> clazz) {
        for (AbstractWindow window : this.windows) {
            if (window.getClass().isAssignableFrom(clazz)) {
                return clazz.cast(window);
            }
        }

        return null;
    }

    public void renderAll(float menuBarHeight) {
        for (AbstractWindow window : this.windows) {
            window.render(menuBarHeight);
        }
    }
}
