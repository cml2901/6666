package dev.eredrin.abilities.common;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Простейший планировщик отложенных действий в тиках сервера. */
public final class ServerScheduler {
    private record Task(long runAt, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    private ServerScheduler() {}

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(now + Math.max(0, delayTicks), action));
    }

    public static long now() {
        return now;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        now++;
        if (TASKS.isEmpty()) {
            return;
        }
        List<Task> due = new ArrayList<>();
        for (Task task : TASKS) {
            if (task.runAt() <= now) {
                due.add(task);
            }
        }
        TASKS.removeAll(due);
        for (Task task : due) {
            try {
                task.action().run();
            } catch (RuntimeException ignored) {
                // не роняем сервер из-за ошибки в одной способности
            }
        }
    }
}
