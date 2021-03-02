package de.phil;

import java.time.Duration;
import java.util.*;

public class TaskRunSimulator {
    private final List<Task> tasks;
    private final List<Task> parallelTaskList = new ArrayList<>();
    private final Set<Integer> tasksDoneIds = new NotOrderedSet<>();
    private final Set<Integer> taskOrderDoneIds = new NotOrderedSet<>();
    private final Map<Duration, Integer> waitIntervals = new HashMap<>();
    private Duration totalDuration = Duration.ZERO;

    public TaskRunSimulator(List<Task> tasks) {
        this.tasks = tasks;
    }

    public ScheduleResult run() {
        // handle single task
        if (tasks.size() == 1) {
            totalDuration = tasks.get(0).getDuration();
        }
        // handle multiple tasks
        else {
            for (Task task : tasks) {

                // keep adding as many parallel tasks as possible
                if (task.isParallel() && taskDependenciesSatisfied(task) && !parallelTaskList.contains(task)) {
                    taskOrderDoneIds.add(task.getId());
                    parallelTaskList.add(task);
                    continue;
                }

                // check if our current task can be done.
                // if not, fulfill it's dependencies first
                if (!taskDependenciesSatisfied(task)) {
                    List<Task> taskDependencies = fetchParallelTaskDependencies(task);
                    Duration waitTime = calculateWaitTime(taskDependencies);

                    trackNewWaitInterval(waitTime);

                    // add wait time to total duration
                    totalDuration = totalDuration.plus(waitTime);
                    meanwhileDoParallelTasks(waitTime);

                    setTasksDone(taskDependencies);

                    // task dependencies are already removed in meanwhileDoParallelTasks
                    // start new available parallel tasks

                    List<Task> newAvailableParallelTasks = fetchAvailableParallelTasks();
                    // for t in newAvail..-> if t == task remove t from newAvail..

                    /*
                    for (int i = 0; i < newAvailableParallelTasks.size(); i++) {
                        if (newAvailableParallelTasks.get(i) == task) {
                            newAvailableParallelTasks.remove(task);
                            i--;
                        }
                    }
                     */

                    // start new available tasks
                    parallelTaskList.addAll(newAvailableParallelTasks);
                    newAvailableParallelTasks.forEach(t -> taskOrderDoneIds.add(t.getId()));

                    if (task.isParallel())
                        continue;
                }

                doSequentialWork(task);
            }
        }

        return new ScheduleResult(totalDuration, new ArrayList<>(taskOrderDoneIds), 1, waitIntervals);
    }

    private void doSequentialWork(Task task) {
        // this method is only meant for sequential tasks
        if (!task.isParallel()) {
            doSequentialTask(task);
            meanwhileDoParallelTasks(task.getDuration());
            setTaskDone(task);

            // task dependencies are already removed in meanwhileDoParallelTasks
            // start new available parallel tasks

            List<Task> newAvailableParallelTasks = fetchAvailableParallelTasks();

            // start new available tasks
            parallelTaskList.addAll(newAvailableParallelTasks);
            newAvailableParallelTasks.forEach(t -> taskOrderDoneIds.add(t.getId()));
        }
    }

    private List<Task> fetchAvailableParallelTasks() {
        List<Task> newAvailableTasks = new ArrayList<>();
        for (Task task : tasks) {
            // if task is parallel and not done, and dependencies have to be done
            // it also must not be in parallel tasks and being started already
            if (task.isParallel() && !tasksDoneIds.contains(task.getId()) && taskDependenciesSatisfied(task) && !parallelTaskList.contains(task)) {
                newAvailableTasks.add(task);
            }
        }
        return newAvailableTasks;
    }

    private void trackNewWaitInterval(Duration waitTime) {
        waitIntervals.put(waitTime, waitIntervals.getOrDefault(waitTime, 0) + 1);
    }

    private void setTaskDone(Task task) {
        tasksDoneIds.add(task.getId());
    }

    private void setTasksDone(List<Task> tasks) {
        for (Task task : tasks) {
            tasksDoneIds.add(task.getId());
        }
    }

    private Duration calculateWaitTime(List<Task> taskDependencies) {
        Duration time = Duration.ZERO;
        for (Task task : taskDependencies) {
            // if task duration > time
            if (task.getDuration().compareTo(time) > 0) {
                time = task.getDuration();
            }
        }
        return time;
    }

    private List<Task> fetchParallelTaskDependencies(Task task) {

        assert(task.hasDependentTasks());

        List<Task> parallelDependencies = new ArrayList<>();
        for (Task t : tasks) {
            if (t.isParallel() && task.getDependentTaskIds().contains(t.getId())) {
                parallelDependencies.add(t);
            }
        }
        return parallelDependencies;
    }

    private void meanwhileDoParallelTasks(Duration duration) {
        for (int i = 0; i < parallelTaskList.size(); i++) {
            Task task = parallelTaskList.get(i);
            task.decreaseDuration(duration);
            if (task.getDuration().isZero()) {
                parallelTaskList.remove(task);
                tasksDoneIds.add(task.getId());
                i--;
            }
        }
    }

    private void doSequentialTask(Task task) {
        taskOrderDoneIds.add(task.getId());
        totalDuration = task.getDuration().plus(totalDuration);
    }

    private boolean taskDependenciesSatisfied(Task task) {
        if (!task.hasDependentTasks())
            return true;
        for (int dep : task.getDependentTaskIds())
            if (!tasksDoneIds.contains(dep))
                return false;
        return true;
    }
}
