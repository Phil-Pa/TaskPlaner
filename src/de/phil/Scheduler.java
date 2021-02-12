package de.phil;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Scheduler {

    private final List<List<Task>> combinations = new ArrayList<>();
    private List<Task> tasks;

    public ScheduleResult scheduleTasks(List<Task> tasks) {
        // TODO: refactor to pass this to constructor
        this.tasks = tasks;

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

    private static List<List<Task>> buildTaskCombinations(List<Task> tasks) {
        List<List<Task>> combinations = new ArrayList<>();

        Map<Integer, List<Task>> levelToPermutationMap = new HashMap<>();

        for (Task task : tasks) {
            int level = getTreeLevel(tasks, task);
            if (!levelToPermutationMap.containsKey(level))
                levelToPermutationMap.put(level, new ArrayList<>());

            levelToPermutationMap.get(level).add(task);
        }

        Map<Integer, Permutations<Task>> levelPermutations = new HashMap<>(levelToPermutationMap.size());

        for (var entry : levelToPermutationMap.entrySet()) {
            levelPermutations.put(entry.getKey(), new Permutations<>(entry.getValue()));
        }

        int deepestLevel = 0;
        for (var level : levelPermutations.keySet()) {
            if (deepestLevel < level)
                deepestLevel = level;
        }

//        for (var entry : levelPermutations.entrySet()) {
//            foo1(entry.getValue(), combinations, deepestLevel, levelPermutations);
//        }

        __foo__(levelPermutations.entrySet().stream().findFirst().get().getValue(), combinations, 1, deepestLevel, levelPermutations, null, tasks.size(), 1);

//        for (Permutations<Task> levelPermutation : levelPermutations.values()) {
//            while (levelPermutation.hasNext()) {
//                List<Task> permutation = levelPermutation.next();
//
//            }
//        }

//        Permutations<Task> e = levelPermutations.get(5);
//
//        Permutations<Task> a;
//        Permutations<Task> b;
//        Permutations<Task> c;
//        Permutations<Task> d;

        // 4 durchläufe
//
//        while (a.hasNext()) {
//            while (b.hasNext()) {
//                while (c.hasNext()) {
//                    while (d.hasNext()) {
//                        var nextA = a.next();
//                        var nextB = b.next();
//                        var nextC = c.next();
//                        var nextD = d.next();
//
//                        List<Task> res = new ArrayList<>();
//
//                        for (Task t1 : nextA) {
//                            for (Task t2 : nextB) {
//                                for (Task t3 : nextC) {
//                                    for (Task t4 : nextD) {
//                                        res.add(t1);
//                                        res.add(t2);
//                                        res.add(t3);
//                                        res.add(t4);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        return combinations;
    }

    private static void __foo__(Permutations<Task> value, List<List<Task>> combinations, int level, int deepestLevel, Map<Integer, Permutations<Task>> levelPermutations, List<Task> perm, int numTasks, int rec) {

        if (perm == null) {
            perm = new ArrayList<>();
        }

        if (level == 1) {
            while (value.hasNext()) {
                if (perm.size() >= 8) {
                    perm.clear();
                }
                perm.addAll(value.next());

                if (levelPermutations.containsKey(level + 1)) {
                    __foo__(levelPermutations.get(level + 1), combinations, level + 1, deepestLevel, levelPermutations, perm, numTasks, rec);
                }
            }
        } else {
            if (value.hasNext()) {
                perm.addAll(value.next());

                if (levelPermutations.containsKey(level + 1)) {
                    __foo__(levelPermutations.get(level + 1), combinations, level + 1, deepestLevel, levelPermutations, perm, numTasks, rec);
                }
            }
        }

        if (!combinations.contains(perm) && perm.size() == numTasks) {
            List<Task> deepCopy = new ArrayList<>();
            for (Task t : perm) {
                deepCopy.add(Task.deepCopy(t));
            }
            combinations.add(deepCopy);
            perm.clear();
        }

        if (!perm.isEmpty())
            perm.clear();

//        if (perm.size() < numTasks) {
//            foo1(value, combinations, level - ++rec, deepestLevel, levelPermutations, perm, numTasks, rec);
//        }

    }

    private static void bar(List<List<Task>> combinations, Map<Integer, Permutations<Task>> levelPermutations, int currentLevel) {
        if (currentLevel < 1)
            return;

        Permutations<Task> permutations = levelPermutations.get(currentLevel);



    }

    private static int getTreeLevel(List<Task> tasks, Task task) {
        int level = 1;

        List<Integer> ids = task.getDependentTaskIds();
        List<Task> dependentTasks = new ArrayList<>();
        for (int id : tasks.stream().map(Task::getId).collect(Collectors.toList())) {
            if (ids == null)
                return level;
            for (int i : ids) {
                if (id == i) {
                    dependentTasks.addAll(tasks.stream().filter(it -> it.getId() == i).collect(Collectors.toList()));
                }
            }
        }

        if (dependentTasks.isEmpty()) {
            return level;
        }

        for (Task t : dependentTasks) {
            level += getTreeLevel(dependentTasks, t);
        }

        return level;
    }

    private List<List<Task>> __bar__(List<Task> tasks) {

        Task leaf = findLeaf(tasks);

        List<Task> permutation = new ArrayList<>();
        rec(leaf, permutation);

        return combinations;
    }

    private void rec(Task leaf, List<Task> permutation) {
        List<Task> dependencies = tasks.stream().filter(it -> {
            if (it.hasDependentTasks()) {
                return leaf.getDependentTaskIds().contains(it.getId());
            } else {
                return false;
            }
        }).collect(Collectors.toList());

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

    // a bit efficient, but not really
    private static List<List<Task>> foo(List<Task> tasks) {
        List<List<Task>> combinations = new ArrayList<>();

        List<Task> leaves = getLeaves(tasks);
        Task root = getRoot(tasks);

        List<Task> middleTasks = new ArrayList<>(tasks);

        middleTasks.remove(root);
        middleTasks.removeAll(leaves);

        Permutations<Task> leafPermutations = new Permutations<>(leaves);
        Permutations<Task> middlePermutations = new Permutations<>(middleTasks);

        List<Integer> tasksDoneIds = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++)
            tasksDoneIds.add(-1);

        while (leafPermutations.hasNext()) {
            List<Task> leafPerm = leafPermutations.next();
            while (middlePermutations.hasNext()) {
                List<Task> middlePerm = middlePermutations.next();
                List<Task> perm = new ArrayList<>(leafPerm.size() + middlePerm.size() + 1);
                perm.addAll(leafPerm.stream().map(Task::deepCopy).collect(Collectors.toList()));
                perm.addAll(middlePerm.stream().map(Task::deepCopy).collect(Collectors.toList()));
                perm.add(Task.deepCopy(root));

                if (isDoable(perm, tasksDoneIds))
                    combinations.add(perm);
            }
        }

        return combinations;
    }

    private static Task getRoot(List<Task> tasks) {
        return tasks.stream().max(Comparator.comparingInt(Task::getId)).orElseThrow();
    }

    private static List<Task> getLeaves(List<Task> tasks) {
        return tasks.stream().filter(t -> !t.hasDependentTasks()).collect(Collectors.toList());
    }

    // not very efficient
    private static List<List<Task>> buildTaskPermutations(List<Task> tasks) {
        // init tasks done ids
        List<Integer> tasksDoneIds = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++)
            tasksDoneIds.add(-1);

        List<List<Task>> lists = new ArrayList<>();
        Permutations<Task> permutations = new Permutations<>(tasks);

        int count = 0;

        while (permutations.hasNext()) {
            List<Task> perm = permutations.next();

            Collections.fill(tasksDoneIds, -1);


            if (isDoable(perm, tasksDoneIds)) {
                List<Task> deepCopy = new ArrayList<>(perm.size());
                for (Task task : perm)
                    deepCopy.add(Task.deepCopy(task));
                lists.add(new ArrayList<>(deepCopy));
            }

            count++;
        }

        System.out.printf("%d/%d valid permutations\n", lists.size(), count);

        return lists;
    }

    private static boolean isDoable(List<Task> perm, List<Integer> taskIdsDone) {

        int currentTaskNum = 0;

        for (Task task : perm) {
            // if dependent tasks are done
            if (task.hasDependentTasks()) {

                if (currentTaskNum == 0)
                    return false;

                for (int id : task.getDependentTaskIds())  {
                    if (!taskIdsDone.contains(id))
                        return false;
                }
            }

            if (taskIdsDone.size() > currentTaskNum) {
                taskIdsDone.set(currentTaskNum++, task.getId());
            }
        }

        return currentTaskNum == taskIdsDone.size();
    }

}
