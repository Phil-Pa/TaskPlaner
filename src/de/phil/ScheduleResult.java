package de.phil;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ScheduleResult {

    private final Duration totalDuration;
    private final List<Integer> orderedTasksIds;
    private final Map<Duration, Integer> waitIntervals;
    private int numPossiblePaths;

    public ScheduleResult(Duration totalDuration, List<Integer> orderedTasksIds, int numPossiblePaths, Map<Duration, Integer> waitIntervals) {
        this.totalDuration = totalDuration;
        this.orderedTasksIds = orderedTasksIds;
        this.numPossiblePaths = numPossiblePaths;
        this.waitIntervals = waitIntervals;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public List<Integer> getOrderedTasksIds() {
        return orderedTasksIds;
    }

    public int getNumPossiblePaths() {
        return numPossiblePaths;
    }

    public Map<Duration, Integer> getWaitIntervals() {
        return waitIntervals;
    }

    public void setNumPossiblePaths(int numPossiblePaths) {
        this.numPossiblePaths = numPossiblePaths;
    }
}
