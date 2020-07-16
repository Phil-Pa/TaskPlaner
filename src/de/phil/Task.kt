package de.phil

import java.time.Duration
import kotlin.random.Random

data class Task(val description: String, val duration: Duration, val isParallel: Boolean, val dependentTasksIds: Array<Long>? = null) {

    val id: Long = ++idCounter
    val hasDependencies = dependentTasksIds != null

    init {
        taskLookup[id] = this
    }

    companion object {
        fun random() = Task("", Duration.ofMillis(Random.nextLong()), Random.nextBoolean())
        private var idCounter: Long = 0

        private val taskLookup = mutableMapOf<Long, Task>()
        fun getTaskById(id: Long) = taskLookup[id]!!
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Task)
            return id == other.id
        return false
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + isParallel.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}