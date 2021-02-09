package de.phil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskRunSimulator {
    private final List<Task> tasks;

    public TaskRunSimulator(List<Task> tasks) {
        this.tasks = tasks;
    }

    public ScheduleResult run() {

        List<Integer> tasksDoneIds = new ArrayList<>();
        List<Task> parallelTasks = new ArrayList<>();

        List<Task> parallelTaskCache = new ArrayList<>();

        Duration totalDuration = Duration.ZERO;
        Map<Duration, Integer> waitIntervals = new HashMap<>();

        for (Task task : tasks) {
            if (task.isParallel() && dependenciesSatisfied(task, tasksDoneIds)) {
                parallelTasks.add(task);
                continue;
            } else if (task.isParallel() && !dependenciesSatisfied(task, tasksDoneIds)) {
                if (dependenciesInParallelTasks(task, parallelTasks)) {
                    parallelTaskCache.add(task);
                    continue;
                }
            } else if (!task.isParallel() && !dependenciesInParallelTasks(task, parallelTasks)) {
                // clear parallel task cache

                decreaseParallelTasksDuration(tasksDoneIds, parallelTasks, task.getDuration());
            }

            while (dependenciesSatisfied(task, tasksDoneIds)) {
                Duration waitDuration = getMinWaitDuration(parallelTasks);
                if (waitDuration == null)
                    break;

                if (waitIntervals.containsKey(waitDuration)) {
                    totalDuration = totalDuration.plus(waitDuration);
                    waitIntervals.put(waitDuration, waitIntervals.get(waitDuration) + 1);
                } else {
                    totalDuration = totalDuration.plus(waitDuration);
                    waitIntervals.put(waitDuration, 1);
                }

                decreaseParallelTasksDuration(tasksDoneIds, parallelTasks, waitDuration);
            }

            decreaseParallelTasksDuration(tasksDoneIds, parallelTasks, task.getDuration());

            tasksDoneIds.add(task.getId());
            totalDuration = totalDuration.plus(task.getDuration());
        }

        Duration maxDuration = Duration.ZERO;
        for (Duration duration : parallelTasks.stream().map(Task::getDuration).collect(Collectors.toList())) {
            if (duration.compareTo(maxDuration) > 0) {
                maxDuration = duration;
            }
        }

        if (!maxDuration.isZero())
            totalDuration = totalDuration.plus(maxDuration);

        return new ScheduleResult(totalDuration, tasks.stream().map(Task::getId).collect(Collectors.toList()), false, waitIntervals);
    }

    private boolean dependenciesInParallelTasks(Task task, List<Task> parallelTasks) {

        if (!task.hasDependentTasks())
            return false;

        for (int id : task.getDependentTaskIds()) {

            boolean idCovered = false;

            for (Task parallelTask : parallelTasks) {
                if (parallelTask.getId() == id) {
                    idCovered = true;
                    break;
                }
            }

            if (!idCovered)
                return false;
        }

        return true;
    }

    private void decreaseParallelTasksDuration(List<Integer> tasksDoneIds, List<Task> parallelTasks, Duration duration) {

        for (int i = 0; i < parallelTasks.size(); i++) {
            Task parallelTask = parallelTasks.get(i);

            // if dependencies are met we can decrease the duration
            parallelTask.decreaseDuration(duration);
            if (parallelTask.getDuration().isZero()) {
                parallelTasks.remove(parallelTask);
                tasksDoneIds.add(parallelTask.getId());
            }

        }
    }

    private Duration getMinWaitDuration(List<Task> parallelTasks) {
        return parallelTasks.stream().map(Task::getDuration).sorted().findFirst().orElse(null);
    }

    private boolean dependenciesSatisfied(Task task, List<Integer> tasksDoneIds) {

        if (!task.hasDependentTasks())
            return true;

        for (int dependencyId : task.getDependentTaskIds()) {
            if (!tasksDoneIds.contains(dependencyId))
                return false;
        }
        return true;
    }
}
