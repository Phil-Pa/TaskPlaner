package de.phil

import java.time.Duration
import kotlin.math.max

class TaskPlaner {

    fun plan(tasks: List<Task>): List<Task> {

        if (tasks.isEmpty())
            return listOf()

        checkCircularTaskDependencies(tasks)

        if (tasks.size == 1)
            return tasks

        if (tasksAreDependent(tasks))
            return planDependentTasks(tasks)

        return planNotDependentTasks(tasks)
    }

    private fun planNotDependentTasks(tasks: List<Task>): MutableList<Task> {
        val parallelTasks = tasks.filter { it.isParallel }.sortedBy { it.duration }
        val nonParallelTasks = tasks.filter { !it.isParallel }.sortedBy { it.duration }

        val result = mutableListOf<Task>()
        result.addAll(parallelTasks)
        result.addAll(nonParallelTasks)

        return result
    }

    private fun planDependentTasks(tasks: List<Task>): List<Task> {
        val first = tasks.first()
        val second = tasks.last()

        if (first.hasDependencies)
            return listOf(second, first)

        return tasks

//        val paths = buildAllPaths(tasks)
//
//        val pathDurationMap = mutableMapOf<List<Task>, Duration>()
//
//        for (path in paths) {
//            pathDurationMap[path] = getPathDuration(path)
//        }
//
//        val minDuration = pathDurationMap.minBy { it.value }!!.value
//        return pathDurationMap.filter { it.value == minDuration }.map { it.key }.first()
    }

    private fun isChain(path: List<Task>): Boolean {
        if (path.first().hasDependencies)
            return false

        val others = path.filterIndexed { index, _ -> index != 0 }
        return others.all { it.hasDependencies && it.dependentTasksIds!!.size == 1 }
    }

    private fun startersAreParallel(path: List<Task>): Boolean {
        val starters = path.filter { !it.hasDependencies }
        return starters.all { it.isParallel }
    }

    fun getPathDuration(path: List<Task>): Duration {

        if (path.isEmpty())
            return Duration.ZERO

        if (path.size == 1)
            return path.first().duration

        if (path.size == 2) {
            if (path.all { !it.isParallel })
                return sumTaskDuration(path)
            return path.maxBy { it.duration }!!.duration
        }

        if (path.size == 3) {
            if (path.last().hasDependencies && path.last().dependentTasksIds!!.size == 2) {
                val firstDuration = getPathDuration(path - path.last())
                return firstDuration + path.last().duration
            }
        }

        if (isChain(path))
            return sumTaskDuration(path)

        val starters = getStarters(path)
        if (startersAreParallel(starters) && !hasParallelPeek(starters)) {
            val others = path - starters
            return starters.maxBy { it.duration }!!.duration + sumTaskDuration(others)
        }

        val tasksDone = mutableListOf<Task>()
        val list = path.toMutableList()
        var duration = Duration.ZERO

        while (list.isNotEmpty()) {

            if (areStarters(list)) {
                duration += getStartersDuration(list)
                break
            }

            val current = list.removeAt(list.size - 1)

            if (list.isEmpty()) {
                duration += current.duration
                break
            }

            val previous = list[list.size - 1]

            if (previousTaskCanBeOmitted(previous, current))
                list.removeAt(list.size - 1)

            if (current.hasDependencies) {
                val dependencies = current.dependentTasksIds!!.map { Task.getTaskById(it) }
                if (dependencies.all { it.isParallel && !it.hasDependencies }) {
                    val others = starters - dependencies
                    val othersDuration = getPathDuration(others)
                    val thisDuration = getPathDuration(dependencies + current)
                    val maxDuration = if (othersDuration > thisDuration) othersDuration else thisDuration
                    duration += maxDuration
                    list.remove(current)
                    list.removeAll(others)
                    list.removeAll(dependencies)
                    continue
                }
            }

            if ((list - current).all { previousTaskCanBeOmitted(it, current) })
                break

            if (areStarters(current, previous)
                    && areParallelAndSequentialTasks(current, previous)
                    && previous !in tasksDone) {
                duration += previous.duration
            } else {
                duration += current.duration
            }

        }

        return duration
    }

    private fun hasParallelPeek(starters: List<Task>): Boolean {
        assert(startersAreParallel(starters))

        for (starter in starters) {
            val others = starters - starter
            if (starter.duration > sumTaskDuration(others))
                return true
        }

        return false
    }

    private fun getStartersDuration(list: MutableList<Task>): Duration {
        val parallelDuration = list.filter { it.isParallel }.maxBy { it.duration }!!.duration
        val nonParallelDuration = sumTaskDuration(list.filter { !it.isParallel })
        return if (parallelDuration > nonParallelDuration) parallelDuration else nonParallelDuration
    }

    private fun areParallelAndSequentialTasks(task1: Task, task2: Task): Boolean {
        if (task1.isParallel && task2.isParallel)
            return false

        if (!task1.isParallel && !task2.isParallel)
            return false

        return true
    }

    private fun areStarters(vararg tasks: Task) = tasks.all { !it.hasDependencies }
    private fun areStarters(tasks: List<Task>) = tasks.all { !it.hasDependencies }

    private fun previousTaskCanBeOmitted(previous: Task, current: Task) =
            previous.isParallel && previous.duration <= current.duration && current.dependentTasksIds?.contains(previous.id)!!

    private fun getStarters(path: List<Task>) = path.filter { !it.hasDependencies }

    private fun sumTaskDuration(path: List<Task>): Duration {
        var sum = Duration.ZERO
        for (task in path)
            sum += task.duration
        return sum
    }

    private fun buildAllPaths(tasks: List<Task>): List<List<Task>> {
        return listOf()
    }

    private fun tasksAreDependent(tasks: List<Task>) = tasks.any { it.hasDependencies }

    private fun checkCircularTaskDependencies(tasks: List<Task>) {
        // TODO
        if (tasks.all { it.hasDependencies })
            throw CircularTaskReferenceException()
    }

}