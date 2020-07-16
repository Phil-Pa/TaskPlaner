package de.phil

import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Method
import java.time.Duration

fun assertEqualsMixedList(expected: Any, obj: Any, method: Method, arguments: List<Any>) {
    val count = arguments.size * arguments.size + 10
    for (i in 1..count) {
        val actual = method.invoke(obj, arguments.shuffled())
        assertEquals(expected, actual)
    }
}

class TaskPlanerNoDependenciesTest {

    private val planer = TaskPlaner()

    @Test
    fun testNoArguments() {
        assertEquals(listOf<Task>(), planer.plan(listOf()))
    }

    @Test
    fun testSingleArgument() {
        val task = Task("", Duration.ofMinutes(10), false)
        assertEquals(listOf(task), planer.plan(listOf(task)))
    }

    @Test
    fun test1Parallel1NonParallel() {
        val nonParallelTask = Task("", Duration.ofMinutes(10), false)
        val parallelTask = Task("", Duration.ofMinutes(20), true)

        assertEquals(listOf(parallelTask, nonParallelTask), planer.plan(listOf(nonParallelTask, parallelTask)))
        assertEquals(listOf(parallelTask, nonParallelTask), planer.plan(listOf(parallelTask, nonParallelTask)))
    }

    @Test
    fun test2NonParallel() {
        val nonParallelTask1 = Task("", Duration.ofMinutes(10), false)
        val nonParallelTask2 = Task("", Duration.ofMinutes(20), false)

        assertEquals(listOf(nonParallelTask1, nonParallelTask2), planer.plan(listOf(nonParallelTask1, nonParallelTask2)))
        assertEquals(listOf(nonParallelTask1, nonParallelTask2), planer.plan(listOf(nonParallelTask2, nonParallelTask1)))
    }

    @Test
    fun test2Parallel() {
        val parallelTask1 = Task("", Duration.ofMinutes(10), true)
        val parallelTask2 = Task("", Duration.ofMinutes(20), true)

        assertEquals(listOf(parallelTask1, parallelTask2), planer.plan(listOf(parallelTask1, parallelTask2)))
        assertEquals(listOf(parallelTask1, parallelTask2), planer.plan(listOf(parallelTask2, parallelTask1)))
    }

    @Test
    fun test3ParallelAnd3NonParallel() {
        var task1 = Task("", Duration.ofMinutes(10), false)
        var task2 = Task("", Duration.ofMinutes(20), false)
        var task3 = Task("", Duration.ofMinutes(30), false)

        assertEquals(listOf(task1, task2, task3),
            planer.plan(listOf(task1, task2, task3)))

        task1 = Task("", Duration.ofMinutes(10), true)
        task2 = Task("", Duration.ofMinutes(20), true)
        task3 = Task("", Duration.ofMinutes(30), true)

        assertEquals(listOf(task1, task2, task3),
                planer.plan(listOf(task1, task2, task3)))
    }

    @Test
    fun test2NonParallel1Parallel() {
        var task1 = Task("", Duration.ofMinutes(10), false)
        var task2 = Task("", Duration.ofMinutes(20), false)
        var task3 = Task("", Duration.ofMinutes(30), true)

        assertEqualsMixedList(listOf(task3, task1, task2),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))


        task1 = Task("", Duration.ofMinutes(10), false)
        task2 = Task("", Duration.ofMinutes(20), true)
        task3 = Task("", Duration.ofMinutes(30), false)

        assertEqualsMixedList(listOf(task2, task1, task3),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))


        task1 = Task("", Duration.ofMinutes(10), true)
        task2 = Task("", Duration.ofMinutes(20), false)
        task3 = Task("", Duration.ofMinutes(30), false)

        assertEqualsMixedList(listOf(task1, task2, task3),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))
    }

    @Test
    fun test1NonParallel2Parallel() {
        var task1 = Task("", Duration.ofMinutes(10), false)
        var task2 = Task("", Duration.ofMinutes(20), true)
        var task3 = Task("", Duration.ofMinutes(30), true)

        assertEqualsMixedList(listOf(task2, task3, task1),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))


        task1 = Task("", Duration.ofMinutes(10), true)
        task2 = Task("", Duration.ofMinutes(20), false)
        task3 = Task("", Duration.ofMinutes(30), true)

        assertEqualsMixedList(listOf(task1, task3, task2),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))


        task1 = Task("", Duration.ofMinutes(10), true)
        task2 = Task("", Duration.ofMinutes(20), true)
        task3 = Task("", Duration.ofMinutes(30), false)

        assertEqualsMixedList(listOf(task1, task2, task3),
                planer,
                TaskPlaner::class.java.methods.first { it.name.startsWith("plan") },
                listOf(task1, task2, task3))
    }
}