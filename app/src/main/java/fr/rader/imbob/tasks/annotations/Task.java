package fr.rader.imbob.tasks.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Task {

    String value();

    /**
     * Sets a priority for the given task.
     * A high priority means the task will be executed before
     * the other tasks with a smaller priority
     */
    int priority() default 0;
}
