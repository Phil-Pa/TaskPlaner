package de.phil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private final int id;
    private final String description;
    private Duration duration;
    private final boolean isParallel;
    private final List<Integer> dependentTaskIds;

    public Task(int id, List<Integer> dependentTaskIds) {
        this.id = id;
        this.description = "";
        this.duration = Duration.ZERO;
        this.isParallel = false;
        this.dependentTaskIds = dependentTaskIds;
    }

    public Task(int id, String description, Duration duration, boolean isParallel, List<Integer> dependentTaskIds) {
        this.id = id;
        this.description = description;
        this.duration = duration;
        this.isParallel = isParallel;
        this.dependentTaskIds = dependentTaskIds;
    }

    public static Task deepCopy(Task task) {
        return new Task(task.id, task.description, Duration.ofSeconds(task.getDuration().getSeconds(), task.getDuration().getNano()), task.isParallel, task.dependentTaskIds);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean isParallel() {
        return isParallel;
    }

    public List<Integer> getDependentTaskIds() {
        return dependentTaskIds;
    }

    public boolean hasDependentTasks() {
        return this.dependentTaskIds != null;
    }

    public void decreaseDuration(Duration duration) {
        this.duration = this.duration.minus(duration);
        if (this.duration.isNegative())
            this.duration = Duration.ZERO;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", duration=" + duration +
                ", isParallel=" + isParallel +
                '}';
    }

    public static List<Task> getDependentTasks(List<Task> tasks, Task task) {

        List<Task> dependentTasks = new ArrayList<>();

        // must have dependencies, otherwise it would have returned 1
        for (int i : task.getDependentTaskIds()) {
            for (Task t : tasks) {
                if (t.getId() == i) {
                    dependentTasks.add(t);
                    break;
                }
            }
        }
        return dependentTasks;
    }
}
