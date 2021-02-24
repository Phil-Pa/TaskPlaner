package de.phil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {

    private final List<List<Task>> combinations = new ArrayList<>();
    private final List<Task> tasks;

    public Scheduler(Task... tasks) {
        this.tasks = Arrays.stream(tasks).collect(Collectors.toList());
    }

    public ScheduleResult scheduleTasks() {

        if (tasks.stream().noneMatch(Task::hasDependentTasks)) {
            return handleNoDependentTasks(tasks);
        }

        List<ScheduleResult> results = new ArrayList<>();

        List<List<Task>> permutations = SchedulerUtils.buildPermutations(tasks); //buildTaskPermutations(tasks);

        for (List<Task> list : permutations) {
            TaskRunSimulator simulator = new TaskRunSimulator(list);
            ScheduleResult result = simulator.run();
            results.add(result);
        }

        // if there are multiple best duration, sort for the duration with the highest wait time
        // and then for length of the intervals (higher is better)

        Duration bestDuration = Duration.ofDays(1);
        int bestDurationIndex = 0;

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getTotalDuration().minus(bestDuration).isNegative()) {
                bestDuration = results.get(i).getTotalDuration();
                bestDurationIndex = i;
            }
        }

        ScheduleResult result = results.get(bestDurationIndex);
        result.setNumPossiblePaths(results.size());

        System.out.println("possible paths: " + result.getNumPossiblePaths());

        return result;
    }

    private static ScheduleResult scheduleParallelTasks(List<Task> tasks) {
        Duration maxDuration = Duration.ZERO;
        for (Task task : tasks) {
            if (task.getDuration().compareTo(maxDuration) > 0) {
                maxDuration = task.getDuration();
            }
        }
        // TODO: handle multiple results return value
        return new ScheduleResult(maxDuration, tasks.stream().map(Task::getId).collect(Collectors.toList()), 1, new HashMap<>());
    }

    private static ScheduleResult scheduleSequentialTasks(List<Task> tasks) {
        Duration duration = Duration.ZERO;
        for (Task task : tasks) {
            duration = duration.plus(task.getDuration());
        }
        // TODO: handle multiple results return value
        return new ScheduleResult(duration, tasks.stream().map(Task::getId).collect(Collectors.toList()), 1, new HashMap<>());
    }

    private static ScheduleResult handleNoDependentTasks(List<Task> tasks) {
        // handle parallel tasks
        if (tasks.stream().allMatch(Task::isParallel)) {
            return scheduleParallelTasks(tasks);
        } else if (tasks.stream().noneMatch(Task::isParallel)) {
            return scheduleSequentialTasks(tasks);
        } else {
            // tasks must be mixed

            List<Task> parallelTasks = tasks.stream().filter(Task::isParallel).collect(Collectors.toList());
            List<Task> sequentialTasks = tasks.stream().filter(it -> !it.isParallel()).collect(Collectors.toList());

            Duration parallelDuration = scheduleParallelTasks(parallelTasks).getTotalDuration();
            Duration sequentialDuration = scheduleSequentialTasks(sequentialTasks).getTotalDuration();

            Duration totalDuration;
            if (parallelDuration.compareTo(sequentialDuration) > 0) {
                totalDuration = parallelDuration;
            } else {
                totalDuration = sequentialDuration;
            }

            List<Integer> orderIds = new ArrayList<>();
            orderIds.addAll(parallelTasks.stream().map(Task::getId).collect(Collectors.toList()));
            orderIds.addAll(sequentialTasks.stream().map(Task::getId).collect(Collectors.toList()));

            // TODO: handle multiple results return value
            return new ScheduleResult(totalDuration, orderIds, 1, new HashMap<>());
        }
    }

}
