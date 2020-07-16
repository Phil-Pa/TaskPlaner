package de.phil

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration

class TaskTest {

    @Test
    fun testDifferentIdAndNotEquals() {
        for (i in 1..100) {
            val a = Task.random()
            val b = Task.random()

            assertNotEquals(a.id, b.id)
            assertNotEquals(a, b)
        }
    }

    @Test
    fun testEquals() {
        val task = Task.random()
        assertEquals(task, task)
    }

    @Test
    fun testHasTaskDependencies() {
        val task = Task("", Duration.ofMillis(1), true, null)
        assertFalse(task.hasDependencies)

        val dependentTask = Task("", Duration.ofMillis(1), true, arrayOf(task.id))
        assertTrue(dependentTask.hasDependencies)
    }

}