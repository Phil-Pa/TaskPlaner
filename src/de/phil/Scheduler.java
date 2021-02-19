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

        List<List<Task>> permutations = __bar__(tasks); //buildTaskPermutations(tasks);

        for (List<Task> list : permutations) {
            TaskRunSimulator simulator = new TaskRunSimulator(list);

            try {
                ScheduleResult result = simulator.run();
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                System.out.println("Error in task simulation");
                e.printStackTrace();
            }
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

        return results.get(bestDurationIndex);
    }

    private static ScheduleResult scheduleParallelTasks(List<Task> tasks) {
        Duration maxDuration = Duration.ZERO;
        for (Task task : tasks) {
            if (task.getDuration().compareTo(maxDuration) > 0) {
                maxDuration = task.getDuration();
            }
        }
        // TODO: handle multiple results return value
        return new ScheduleResult(maxDuration, tasks.stream().map(Task::getId).collect(Collectors.toList()), false, new HashMap<>());
    }

    private static ScheduleResult scheduleSequentialTasks(List<Task> tasks) {
        Duration duration = Duration.ZERO;
        for (Task task : tasks) {
            duration = duration.plus(task.getDuration());
        }
        // TODO: handle multiple results return value
        return new ScheduleResult(duration, tasks.stream().map(Task::getId).collect(Collectors.toList()), false, new HashMap<>());
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
            return new ScheduleResult(totalDuration, orderIds, false, new HashMap<>());
        }
    }

    private List<List<Task>> __bar__(List<Task> tasks) {

        Task leaf = findLeaf(tasks);

        List<Task> permutation = new ArrayList<>();
        rec(leaf, permutation);

        return combinations;
    }

    public List<Task> getDependenciesOfTask(Task task) {

        if (!task.hasDependentTasks())
            return new ArrayList<>();

        return tasks.stream().filter(it -> {
            if (it.hasDependentTasks()) {
                return task.getDependentTaskIds().contains(it.getId());
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private void rec(Task leaf, List<Task> permutation) {

        List<Task> dependencies = getDependenciesOfTask(leaf);

        if (!permutation.contains(leaf))
            permutation.add(leaf);

        for (Task dependency : dependencies) {
            //permutation.add(dependency);
            permutation.addAll(dependencies);
            rec(dependency, permutation);
        }

        if (combinations.contains(permutation))
            return;

        List<Task> deepCopy = new ArrayList<>();
        for (Task task : permutation)
            deepCopy.add(Task.deepCopy(task));

        Collections.reverse(deepCopy);

        combinations.add(deepCopy);
        permutation.clear();
    }

    private static Task findLeaf(List<Task> tasks) {
        // first, pick a random root, we choose the first in the list.
        // the first one should always have no dependencies, so it is
        // a safe choice

        // check if some tasks has the root as dependency
        Task taskWithRootAsDependency = tasks.get(0);
        Task lastNode = null;
        while (taskWithRootAsDependency != null) {
            taskWithRootAsDependency = getTaskWithDependency(taskWithRootAsDependency, tasks);
            if (taskWithRootAsDependency != null)
                lastNode = taskWithRootAsDependency;
        }
        return lastNode;
    }

    private static Task getTaskWithDependency(Task task, List<Task> tasks) {
        for (Task t : tasks) {
            if (t.hasDependentTasks() && t.getDependentTaskIds().contains(task.getId())) {
                return t;
            }
        }
        return null;
    }

}
