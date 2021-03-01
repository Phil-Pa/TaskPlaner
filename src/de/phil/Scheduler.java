package de.phil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {

    private static final int MANY_PERMUTATIONS_THRESHOLD = 50000;
    private final List<Task> tasks;

    public Scheduler(Task... tasks) {

        boolean tasksEqualsNullOrNotEmpty = Arrays.stream(tasks).anyMatch(it -> it.getDependentTaskIds() == null || !it.getDependentTaskIds().isEmpty());
        assert(tasksEqualsNullOrNotEmpty);

        this.tasks = Arrays.stream(tasks).collect(Collectors.toList());
    }

    public ScheduleResult scheduleTasks() {

        if (tasks.stream().noneMatch(Task::hasDependentTasks)) {
            return handleNoDependentTasks(tasks);
        }

        TaskSequenceGenerator generator = new TaskSequenceGenerator(tasks, (it) -> System.out.println(it.size()));
        generator.alltopologicalSorts();
        List<List<Task>> permutations = generator.getPermutations();

        if (permutations.size() >= MANY_PERMUTATIONS_THRESHOLD) {
            return scheduleMultiThreaded(permutations);
        } else {
            return scheduleSingleThreaded(permutations);
        }
    }

    private ScheduleResult scheduleMultiThreaded(List<List<Task>> permutations) {
        List<ScheduleResult> results = new ArrayList<>(permutations.size());
        int numThreads = 8; //Thread.activeCount();

        List<Thread> threads = new ArrayList<>();
        List<List<ScheduleResult>> threadResults = new ArrayList<>();

        List<Integer> rangeStart = new ArrayList<>();
        List<Integer> rangeEnd = new ArrayList<>();

        int numPerThread = permutations.size() / numThreads;
        for (int i = 0; i < numThreads - 1; i++) {
            rangeStart.add(i * numPerThread);
            rangeEnd.add((i + 1) * numPerThread - 1);
        }

        rangeStart.add(rangeEnd.get(rangeEnd.size() - 1) + 1);
        rangeEnd.add(permutations.size() - 1);

        for (int i = 0; i < numThreads; i++) {
            threadResults.add(new ArrayList<>(permutations.size() / numThreads + numThreads + 1));
            int finalI = i;
            threads.add(new Thread(() -> {
                int start = rangeStart.get(finalI);
                int end = rangeEnd.get(finalI);

                for (int j = start; j < end; j++) {
                    TaskRunSimulator simulator = new TaskRunSimulator(permutations.get(j));
                    ScheduleResult result = simulator.run();
                    threadResults.get(finalI).add(result);

                    // will gc allow to free the memory
                    permutations.set(j, null);
                }
            }));
            threads.get(i).start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (List<ScheduleResult> tempResults : threadResults) {
            results.addAll(tempResults);
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

    private ScheduleResult scheduleSingleThreaded(List<List<Task>> permutations) {
        List<ScheduleResult> results = new ArrayList<>(permutations.size());
        System.out.println("scheduling single threaded");

        // memory optimization:
        // remove permutation from permutations so that memory gets cleaned
        // and it's not used elsewhere
        while (!permutations.isEmpty()) {
            List<Task> list = permutations.remove(permutations.size() - 1);
            TaskRunSimulator simulator = new TaskRunSimulator(list);
            ScheduleResult result = simulator.run();
            results.add(result);

            if (results.size() % 50000 == 0)
                System.out.println("results: " + results.size());
        }

        // if there are multiple best duration, sort for the duration with the highest wait time
        // and then for length of the intervals (higher is better)

        Duration bestDuration = Duration.ofDays(1);
        int bestDurationIndex = 0;

        System.out.println("calc best duration");

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getTotalDuration().minus(bestDuration).isNegative()) {
                bestDuration = results.get(i).getTotalDuration();
                bestDurationIndex = i;
            }

            if (i % 50000 == 0)
                System.out.println("progress: " + i);
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
