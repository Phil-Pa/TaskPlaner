package de.phil

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class TaskPlanerWithDependenciesTest {

    private val planer = TaskPlaner()

    @Test
    fun testNotEveryTaskHasDependencies() {
        val task = Task("", Duration.ofMillis(1), true, arrayOf(1))

        try {
            planer.plan(listOf(task))
            fail("task planer not throwing CircularTaskReferenceException")
        } catch (e: CircularTaskReferenceException) {

        }
    }

    @Test
    fun testSingleArgument() {
        val task = Task("", Duration.ofMillis(1), true, null)
        assertEquals(listOf(task), planer.plan(listOf(task)))
    }

    @Test
    fun testTwoArgumentsParallel() {
        var a = Task("", Duration.ofMillis(10), true, null)
        var b = Task("", Duration.ofMillis(20), true, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))

        a = Task("", Duration.ofMillis(20), true, null)
        b = Task("", Duration.ofMillis(10), true, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))
    }

    @Test
    fun testTwoArgumentsNonParallel() {
        var a = Task("", Duration.ofMillis(10), false, null)
        var b = Task("", Duration.ofMillis(20), false, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))

        a = Task("", Duration.ofMillis(20), false, null)
        b = Task("", Duration.ofMillis(10), false, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))
    }

    @Test
    fun testTwoArguments1Parallel1NonParallel() {
        var a = Task("", Duration.ofMillis(10), true, null)
        var b = Task("", Duration.ofMillis(20), false, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))


        a = Task("", Duration.ofMillis(20), true, null)
        b = Task("", Duration.ofMillis(10), false, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))


        a = Task("", Duration.ofMillis(20), false, null)
        b = Task("", Duration.ofMillis(10), true, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))


        a = Task("", Duration.ofMillis(10), false, null)
        b = Task("", Duration.ofMillis(20), true, arrayOf(a.id))

        assertEquals(listOf(a, b), planer.plan(listOf(a, b)))
        assertEquals(listOf(a, b), planer.plan(listOf(b, a)))
    }

}