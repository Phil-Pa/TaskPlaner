package de.phil

import java.time.Duration

abstract class Worker {

    protected val taskQueue = mutableListOf<Task>()

    val hasTasksTodo get() = taskQueue.isNotEmpty()
    val currentTask get() = taskQueue.first()

    fun addTask(task: Task) {
        taskQueue.add(task)
    }

    abstract fun advanceTime(duration: Duration)
    protected abstract fun decreaseTaskDuration(task: Task, duration: Duration)
    fun getAdvanceTime() = taskQueue.maxBy { it.duration }!!.duration
    fun removeCurrentTask() {
        taskQueue.removeAt(0)
    }
}

class ParallelWorker : Worker() {
    override fun advanceTime(duration: Duration) {
        if (!hasTasksTodo)
            return

        for (i in taskQueue.indices) {
            val task = taskQueue[i]
            decreaseTaskDuration(task, duration)
        }
    }

    override fun decreaseTaskDuration(task: Task, duration: Duration) {
        if (task.duration <= duration)
            taskQueue.remove(task)
        else
            task.duration = task.duration - duration
    }
}

class SequentialWorker : Worker() {
    override fun advanceTime(duration: Duration) {
        if (!hasTasksTodo)
            return

        val task = taskQueue.first()
        decreaseTaskDuration(task, duration)
    }

    override fun decreaseTaskDuration(task: Task, duration: Duration) {
        if (task.duration > duration)
            task.duration = task.duration - duration
        else {
            val diff = duration - task.duration

            taskQueue.remove(task)

            if (diff > Duration.ZERO)
                advanceTime(diff)
        }
    }

}