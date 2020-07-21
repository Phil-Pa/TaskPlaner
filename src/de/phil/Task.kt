package de.phil

import java.time.Duration
import kotlin.random.Random

class Task(private val description: String, val duration: Duration, val isParallel: Boolean, val dependentTasksIds: Array<Long>? = null) {

    val id: Long = ++idCounter
    val hasDependencies = dependentTasksIds != null
    val isStarterTask = !hasDependencies

    init {
        taskLookup[id] = this
    }

    companion object {
        fun random() = Task("", Duration.ofMillis(Random.nextLong()), Random.nextBoolean())
        private var idCounter: Long = 0

        private val taskLookup = mutableMapOf<Long, Task>()
        private fun getTaskById(id: Long) = taskLookup[id]!!
        fun getDependenciesByIds(ids: Array<Long>): List<Task> {
            val tasks = mutableListOf<Task>()
            ids.forEach { tasks.add(getTaskById(it)) }
            return tasks
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Task)
            return id == other.id
        return false
    }

    override fun toString(): String {
//        val parallel = if (isParallel) 'p' else 's'
        val ids = dependentTasksIds?.joinToString(";")
//        return "$description|$duration|$parallel|($ids)"
        return "$id => $ids"
    }

    override fun hashCode(): Int {
        var result = description.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + isParallel.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}