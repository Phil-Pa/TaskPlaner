package de.phil

class TaskPlaner {

    private fun getNumOfParallelTasks(tasks: List<Task>) = tasks.filter { it.isParallel }.size
    private fun sortByDurationAndPutTaskToListPosition(tasks: List<Task>, taskToMove: Task, position: ListPosition): List<Task> {
        val result = tasks.toMutableList()
        result.remove(taskToMove)
        result.sortBy { it.duration }

        when (position) {
            ListPosition.Start -> result.add(0, taskToMove)
            ListPosition.End -> result.add(taskToMove)
        }

        return result
    }

    fun plan(tasks: List<Task>): List<Task> {

        if (tasks.isEmpty())
            return listOf()

        checkCircularTaskDependencies(tasks)

        if (tasks.size == 1)
            return tasks

        if (tasksAreDependent(tasks))
            return planDependentTasks(tasks)

        when (tasks.size) {
            2 -> return planTwoTasks(tasks)
            3 -> return planThreeTasks(tasks)
        }

        return listOf()
    }

    private fun planThreeTasks(tasks: List<Task>): List<Task> {
        val allTasksAreNotParallel = tasks.all { !it.isParallel }
        if (allTasksAreNotParallel)
            return tasks

        val numTasksParallel = getNumOfParallelTasks(tasks)

        if (numTasksParallel == 1) {
            val parallelTask = tasks.first { it.isParallel }
            return sortByDurationAndPutTaskToListPosition(tasks, taskToMove = parallelTask, position = ListPosition.Start)
        } else if (numTasksParallel == 2) {
            val nonParallelTask = tasks.first { !it.isParallel }
            return sortByDurationAndPutTaskToListPosition(tasks, taskToMove = nonParallelTask, position = ListPosition.End)

        }

        return tasks
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

    private fun planTwoTasks(tasks: List<Task>): List<Task> {
        return if (tasks.first().isParallel || tasks.all { !it.isParallel })
            tasks
        else
            tasks.reversed()
    }

}