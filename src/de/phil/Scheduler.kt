package de.phil

import java.time.Duration

fun Duration.difference(duration: Duration): Duration {
    return if (duration > this)
        duration - this
    else
        this - duration
}

class Scheduler {

    private lateinit var sequentialWorker: Worker
    private lateinit var parallelWorker: Worker
    private var switchedFromParallelToSequential = false

    fun schedule(path: MutableList<Task>): Duration {
        resetStateValues()
        var duration = Duration.ZERO

        while (path.isNotEmpty()) {
            if (sequentialWorker.hasTasksTodo) {
                pullNextSequentialTask(path)
                duration = advanceTime(duration, false)
            }
            else
                duration = doNextTasks(path, duration)
        }

        return duration
    }

    private fun doNextTasks(path: MutableList<Task>, duration: Duration): Duration {
        var temp = duration
        pullNextParallelTasks(path)
        temp = advanceTime(temp, true)

        if (path.isNotEmpty()) {
            pullNextSequentialTask(path)
            temp = advanceTime(temp, false)
        }
        return temp
    }

    private var lastAdvanceWasParallel = false
    private var lastAdvanceTime = Duration.ZERO
    private var savedSequentialTimeDuringParallelTasks = Duration.ZERO

    private fun advanceTime(duration: Duration, advanceParallel: Boolean): Duration {
        var temp = duration

        if (lastAdvanceWasParallel && !advanceParallel) {
            val sequentialTaskDuration = sequentialWorker.currentTask.duration
            sequentialWorker.removeCurrentTask()
            val diff = lastAdvanceTime.difference(sequentialTaskDuration)
            if (diff > lastAdvanceTime)
                temp += diff
            else if (diff + savedSequentialTimeDuringParallelTasks > lastAdvanceTime)
                temp += lastAdvanceTime.difference(diff + savedSequentialTimeDuringParallelTasks)
            else
                savedSequentialTimeDuringParallelTasks += diff
        } else {

            temp += getAdvanceTime()
        }

        lastAdvanceWasParallel = advanceParallel
        lastAdvanceTime = temp
        return temp
    }

    private fun getAdvanceTime(): Duration {
        return if (parallelWorker.hasTasksTodo && !sequentialWorker.hasTasksTodo)
            parallelWorker.getAdvanceTime()
        else if (!parallelWorker.hasTasksTodo && sequentialWorker.hasTasksTodo)
            sequentialWorker.currentTask.duration
        else
            if (parallelWorker.currentTask.duration > sequentialWorker.currentTask.duration)
                parallelWorker.currentTask.duration else sequentialWorker.currentTask.duration
    }

    private fun nextTaskIsParallel(path: List<Task>): Boolean {
        if (path.isEmpty())
            return false
        return path.first().isParallel
    }

    private fun resetStateValues() {
        parallelWorker = ParallelWorker()
        sequentialWorker = SequentialWorker()
        lastAdvanceTime = Duration.ZERO
        lastAdvanceWasParallel = false
        switchedFromParallelToSequential = false
        savedSequentialTimeDuringParallelTasks = Duration.ZERO
    }

    private fun pullNextParallelTasks(path: MutableList<Task>) {
        while (nextTaskIsParallel(path)) {
            val current = pullNextParallelTask(path)
            parallelWorker.addTask(current)
        }
    }

    private fun pullNextSequentialTask(path: MutableList<Task>) {
        val current = path.removeAt(0)
        sequentialWorker.addTask(current)
    }

    private fun pullNextParallelTask(path: MutableList<Task>) = path.removeAt(0)
}