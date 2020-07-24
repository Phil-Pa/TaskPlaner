package de.phil

import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class SchedulerTest {

    @Test
    fun test() {
        val list1 = mutableListOf<Task>()

        val task1 = Task("1", Duration.ofMinutes(20), true)
        val task2 = Task("2", Duration.ofMinutes(5), true)

        list1.add(task1)
        list1.add(task2)

        val scheduler = Scheduler()
        var duration = scheduler.schedule(list1.toMutableList())
        assertEquals(Duration.ofMinutes(20), duration)

        val task3 = Task("3", Duration.ofMinutes(10), false)
        list1.add(task3)
        duration = scheduler.schedule(list1.toMutableList())
        assertEquals(Duration.ofMinutes(20), duration)

        val task4 = Task("4", Duration.ofMinutes(15), false)
        list1.add(task4)
        duration = scheduler.schedule(list1.toMutableList())

        assertEquals(Duration.ofMinutes(25), duration)
    }

}