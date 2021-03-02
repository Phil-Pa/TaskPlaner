package de.phil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {

    private static final int MANY_PERMUTATIONS_THRESHOLD = 5000000;
    private final List<Task> tasks;

    public Scheduler(Task... tasks) {

        if (!isValidTreeDataStructure(Arrays.stream(tasks).collect(Collectors.toList())))
            throw new IllegalArgumentException("tasks are not valid tree");

        boolean tasksEqualsNullOrNotEmpty = Arrays.stream(tasks).anyMatch(it -> it.getDependentTaskIds() == null || !it.getDependentTaskIds().isEmpty());
        assert(tasksEqualsNullOrNotEmpty);

        this.tasks = Arrays.stream(tasks).collect(Collectors.toList());
    }

    private boolean isValidTreeDataStructure(List<Task> tasks) {
        List<Integer> ids = tasks.stream().map(it -> it.getDependentTaskIds() == null ? new ArrayList<Integer>() : it.getDependentTaskIds()).flatMap(Collection::stream).collect(Collectors.toList());

        return (long) ids.size() == ids.stream().distinct().count();
    }

    /**
     * Task Scheduling works fine with a tree data structure, but if possible, general graphs
     * without circles should also be supported. But nodes in graphs, even without circles,
     * have an ambiguous depth, for example:
     * 1 -> 2
     * 1 -> 3
     * 2 -> 3
     * There you could go directly to node 3 with depth 1, or from 1 to 2 and then from 2 to 3
     * with depth 2. Maybe this has no impact to our solution, but it should be aware.
     * @return The result of the simulation going through the tasks in the given sequence.
     */
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

    // TODO: maybe make the method non static and use the tasks member field
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

    private static List<Task> foo(List<Task> tasks, int depth) {
        while (true) {
            List<Task> depthTasks = findNodesWithDepth(tasks, depth);
            if (!depthTasks.isEmpty())
                return depthTasks;
        }
    }

    private static List<Task> findNodesWithDepth(List<Task> tasks, int depth) {

        // assume the tasks build a tree
        Map<Integer, List<Task>> depthToTasksMap = new HashMap<>();

        // TODO: optimize for number of map entries
        for (int i = 1; i <= tasks.size(); i++) {
            depthToTasksMap.put(i, new ArrayList<>());
        }

        for (Task task : tasks) {
            int taskDepth = getTaskDepth(tasks, task);
            depthToTasksMap.get(taskDepth).add(task);
        }

        return new ArrayList<>(depthToTasksMap.get(depth));
    }

    private static List<List<Task>> findSubTrees(List<Task> tasks, int numSubNodes) {
        List<List<Task>> subTrees = new ArrayList<>();
        List<Task> leaves = tasks.stream().filter(it -> !it.hasDependentTasks()).collect(Collectors.toList());

        for (Task task : leaves) {
            if (task.hasDependentTasks()) {

            }
        }

        return null;
    }

    public static int getTaskDepth(List<Task> tasks, Task task) {
        int depth = 1;
        return getTaskDepth(tasks, task, depth);
    }

    private static int getTaskDepth(List<Task> tasks, Task task, int depth) {
        List<Task> leaves = tasks.stream().filter(it -> !it.hasDependentTasks()).collect(Collectors.toList());
        if (leaves.contains(task))
            return depth;

        List<Task> dependentTasks = Task.getDependentTasks(tasks, task);
        for (Task t : dependentTasks) {
            int fooDepth = getTaskDepth(tasks, t, depth);
            return fooDepth + 1;
        }

        // TODO: throw exception, error should not happen because recursion ends at previous return
        // maybe using iterator?
        return -1;
    }

}
