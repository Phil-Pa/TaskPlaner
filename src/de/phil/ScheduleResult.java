package de.phil;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ScheduleResult {

    private final Duration totalDuration;
    private final List<Integer> orderedTasksIds;
    private final boolean multipleResults;
    private final Map<Duration, Integer> waitIntervals;

    public ScheduleResult(Duration totalDuration, List<Integer> orderedTasksIds, boolean multipleResults, Map<Duration, Integer> waitIntervals) {
        this.totalDuration = totalDuration;
        this.orderedTasksIds = orderedTasksIds;
        this.multipleResults = multipleResults;
        this.waitIntervals = waitIntervals;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public List<Integer> getOrderedTasksIds() {
        return orderedTasksIds;
    }

    public boolean hasMultipleResults() {
        return multipleResults;
    }

    public Map<Duration, Integer> getWaitIntervals() {
        return waitIntervals;
    }
}
