package de.phil

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
    }

    private fun tasksAreDependent(tasks: List<Task>) = tasks.any { it.hasDependencies }

    private fun checkCircularTaskDependencies(tasks: List<Task>) {
        if (tasks.all { it.hasDependencies })
            throw CircularTaskReferenceException()
    }

}