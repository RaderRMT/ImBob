package fr.rader.imbob.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import fr.rader.imbob.packets.Packet;
import fr.rader.imbob.packets.PacketAcceptor;
import fr.rader.imbob.tasks.annotations.Task;

public abstract class AbstractTask {

    private final List<PacketAcceptor> acceptors;

    private final String taskName;
    private final int priority;

    protected AbstractTask() {
        this.acceptors = new ArrayList<>();
        
        // we get the @TaskName annotation
        Task taskNameAnnotation = this.getClass().getDeclaredAnnotation(Task.class);

        // if the class doesn't have an annotation,
        // then we simply get the class name
        if (taskNameAnnotation == null) {
            this.taskName = this.getClass().getSimpleName();
            this.priority = 0;
        } else {
            // otherwise we get the annotation's value
            this.taskName = taskNameAnnotation.value();
            this.priority = taskNameAnnotation.priority();
        }
    }

    /**
     * Add a {@link PacketAcceptor} to this task
     *
     * @param acceptor  The {@link PacketAcceptor} to add
     */
    protected final void acceptPacket(PacketAcceptor acceptor) {
        this.acceptors.add(acceptor);
    }

    /**
     * This method uses the {@link Packet} given as a parameter to know if the task can edit the packet.
     * The packet will have its packet id and the protocol from which it came from.
     * A packet is accepted if the execute(Packet) method is designed to edit the packet.
     *
     * @return  true if the packet is accepted by the task, false otherwise.
     */
    final boolean accept(Packet packet) {
        // we loop through each acceptor in the task
        for (PacketAcceptor acceptor : this.acceptors) {
            // if the acceptor accepts the packet, then we return true
            if (acceptor.accept(packet)) {
                return true;
            }
        }

        // if the packet can't be edited by this task,
        // then we return false
        return false;
    }

    /**
     * Execute an edit on the given {@link Packet}.
     * All edits have to be done in this method.
     */
    protected abstract void execute(Packet packet, Queue<Packet> packets);

    /**
     * Render the content of the tast.
     * This is generally used to change the task's
     * parameters so we can affect the final output
     */
    public abstract void render();

    /**
     * Get a user friendly task name from the {@link Task} annotation.
     * If the class is not annotated by a {@link Task} annotation,
     * the class' simple name will be returned.
     *
     * @return  The task's name from the {@link Task} annotation,
     *          or the class' simple name if no annotation is present
     */
    public final String getTaskName() {
        return this.taskName;
    }

    public final int getPriority() {
        return this.priority;
    }
}
