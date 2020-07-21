package de.phil

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream
import kotlin.test.assertEquals

class TaskPlanerGetPathDurationTest {

    private val planer = TaskPlaner()

    @ParameterizedTest
    @MethodSource("generator", "pathChainGenerator", "generator2")
    fun testPaths(path: List<Task>, expectedDurationInMinutes: Long) {
        val actual = planer.getPathDuration(path).toMinutes()
        assertEquals(expectedDurationInMinutes, actual, "path takes actually $actual minutes, but it should be $expectedDurationInMinutes")
    }

    @Suppress("unused")
    companion object {
        @JvmStatic
        fun generator(): Stream<Arguments> {

            val list1 = mutableListOf<Task>()
            val list2 = mutableListOf<Task>()
            val list3 = mutableListOf<Task>()

            var task1 = Task("1", Duration.ofMinutes(20), true)
            var task2 = Task("2", Duration.ofMinutes(5), true)
            var task3 = Task("3", Duration.ofMinutes(10), true)
            var task4 = Task("4", Duration.ofMinutes(10), false, arrayOf(task1.id, task2.id))
            var task5 = Task("5", Duration.ofMinutes(5), false, arrayOf(task2.id, task3.id))
            var task6 = Task("6", Duration.ofMinutes(30), false, arrayOf(task4.id, task5.id))

            list1.add(task1)
            list1.add(task2)
            list1.add(task3)
            list1.add(task4)
            list1.add(task5)
            list1.add(task6)

            task1 = Task("1", Duration.ofMinutes(10), true)
            task2 = Task("2", Duration.ofMinutes(5), false)
            task3 = Task("3", Duration.ofMinutes(20), false)
            task4 = Task("4", Duration.ofMinutes(20), false, arrayOf(task1.id, task2.id))
            task5 = Task("5", Duration.ofMinutes(10), true, arrayOf(task2.id, task3.id))
            task6 = Task("6", Duration.ofMinutes(30), false, arrayOf(task4.id, task5.id))

            list2.add(task1)
            list2.add(task2)
            list2.add(task3)
            list2.add(task4)
            list2.add(task5)
            list2.add(task6)

            task1 = Task("1", Duration.ofMinutes(10), true)
            task2 = Task("2", Duration.ofMinutes(20), false, arrayOf(task1.id))
            task3 = Task("3", Duration.ofMinutes(30), true, arrayOf(task2.id))
            task4 = Task("4", Duration.ofMinutes(40), true, arrayOf(task3.id))
            task5 = Task("5", Duration.ofMinutes(30), false, arrayOf(task4.id))
            task6 = Task("6", Duration.ofMinutes(20), false, arrayOf(task5.id))

            list3.add(task1)
            list3.add(task2)
            list3.add(task3)
            list3.add(task4)
            list3.add(task5)
            list3.add(task6)

            return Stream.of(
                    Arguments.of(list1, 60),
                    Arguments.of(list2, 75),
                    Arguments.of(list3, 150)
            )
        }

        @JvmStatic
        fun pathChainGenerator(): Stream<Arguments> {

            val list1 = mutableListOf<Task>()
            val list2 = mutableListOf<Task>()
            val list3 = mutableListOf<Task>()
            val list4 = mutableListOf<Task>()
            val list5 = mutableListOf<Task>()
            val list6 = mutableListOf<Task>()
            val list7 = mutableListOf<Task>()
            val list8 = mutableListOf<Task>()

            var task1 = Task("", Duration.ofMinutes(1), true)
            var task2 = Task("", Duration.ofMinutes(2), true, arrayOf(task1.id))
            var task3 = Task("", Duration.ofMinutes(3), true, arrayOf(task2.id))
            list1.add(task1)
            list1.add(task2)
            list1.add(task3)

            task1 = Task("", Duration.ofMinutes(1), false)
            task2 = Task("", Duration.ofMinutes(2), false, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), false, arrayOf(task2.id))
            list2.add(task1)
            list2.add(task2)
            list2.add(task3)

            task1 = Task("", Duration.ofMinutes(1), true)
            task2 = Task("", Duration.ofMinutes(2), false, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), false, arrayOf(task2.id))
            list3.add(task1)
            list3.add(task2)
            list3.add(task3)

            task1 = Task("", Duration.ofMinutes(1), false)
            task2 = Task("", Duration.ofMinutes(2), true, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), true, arrayOf(task2.id))
            list4.add(task1)
            list4.add(task2)
            list4.add(task3)

            task1 = Task("", Duration.ofMinutes(1), true)
            task2 = Task("", Duration.ofMinutes(2), true, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), false, arrayOf(task2.id))
            list5.add(task1)
            list5.add(task2)
            list5.add(task3)

            task1 = Task("", Duration.ofMinutes(1), true)
            task2 = Task("", Duration.ofMinutes(2), false, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), true, arrayOf(task2.id))
            list6.add(task1)
            list6.add(task2)
            list6.add(task3)

            task1 = Task("", Duration.ofMinutes(1), false)
            task2 = Task("", Duration.ofMinutes(2), false, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), true, arrayOf(task2.id))
            list7.add(task1)
            list7.add(task2)
            list7.add(task3)

            task1 = Task("", Duration.ofMinutes(1), false)
            task2 = Task("", Duration.ofMinutes(2), true, arrayOf(task1.id))
            task3 = Task("", Duration.ofMinutes(3), false, arrayOf(task2.id))
            list8.add(task1)
            list8.add(task2)
            list8.add(task3)

            return Stream.of(
//                    Arguments.of(list1, 3),
//                    Arguments.of(list2, 6),
//                    Arguments.of(list3, 5),
//                    Arguments.of(list4, 3),
//                    Arguments.of(list5, 3),
//                    Arguments.of(list6, 3),
//                    Arguments.of(list7, 3),
//                    Arguments.of(list8, 4)
                    Arguments.of(list1, 6),
                    Arguments.of(list2, 6),
                    Arguments.of(list3, 6),
                    Arguments.of(list4, 6),
                    Arguments.of(list5, 6),
                    Arguments.of(list6, 6),
                    Arguments.of(list7, 6),
                    Arguments.of(list8, 6)
            )

        }

        @JvmStatic
        fun generator2(): Stream<Arguments> {

            val list1 = mutableListOf<Task>()
            val list2 = mutableListOf<Task>()
            val list3 = mutableListOf<Task>()

            var task1 = Task("2", Duration.ofMinutes(20), true)
            var task2 = Task("3", Duration.ofMinutes(5), true)
            var task3 = Task("1", Duration.ofMinutes(10), true)
            var task4 = Task("5", Duration.ofMinutes(10), false, arrayOf(task1.id, task2.id))
            var task5 = Task("4", Duration.ofMinutes(5), false, arrayOf(task2.id, task3.id, task4.id))
            var task6 = Task("6", Duration.ofMinutes(30), false, arrayOf(task4.id, task5.id))

            list1.add(task1)
            list1.add(task2)
            list1.add(task3)
            list1.add(task4)
            list1.add(task5)
            list1.add(task6)

            task1 = Task("1", Duration.ofMinutes(30), true)
            task2 = Task("2", Duration.ofMinutes(40), false)
            task3 = Task("3", Duration.ofMinutes(10), true)
            task4 = Task("4", Duration.ofMinutes(20), true, arrayOf(task1.id))
            task5 = Task("5", Duration.ofMinutes(20), false, arrayOf(task2.id))
            task6 = Task("6", Duration.ofMinutes(5), true, arrayOf(task3.id))
            var task7 = Task("7", Duration.ofMinutes(10), false, arrayOf(task4.id, task5.id, task6.id))

            list2.add(task1)
            list2.add(task2)
            list2.add(task3)
            list2.add(task4)
            list2.add(task5)
            list2.add(task6)
            list2.add(task7)

            task1 = Task("1", Duration.ofMinutes(10), true)
            task2 = Task("2", Duration.ofMinutes(5), false)
            task3 = Task("3", Duration.ofMinutes(20), true)
            task4 = Task("4", Duration.ofMinutes(20), true, arrayOf(task1.id))
            task5 = Task("5", Duration.ofMinutes(10), false, arrayOf(task2.id, task3.id))
            task6 = Task("6", Duration.ofMinutes(30), false, arrayOf(task3.id))
            task7 = Task("7", Duration.ofMinutes(40), false)
            val task8 = Task("8", Duration.ofMinutes(10), false, arrayOf(task4.id))
            val task9 = Task("9", Duration.ofMinutes(30), false, arrayOf(task4.id, task5.id))
            val task10 = Task("10", Duration.ofMinutes(20), false, arrayOf(task6.id))
            val task11 = Task("11", Duration.ofMinutes(50), true, arrayOf(task6.id, task7.id))
            val task12 = Task("12", Duration.ofMinutes(15), true, arrayOf(task8.id))
            val task13 = Task("13", Duration.ofMinutes(40), false, arrayOf(task9.id, task10.id, task11.id, task12.id))

            list3.add(task1)
            list3.add(task2)
            list3.add(task3)
            list3.add(task4)
            list3.add(task5)
            list3.add(task6)
            list3.add(task7)
            list3.add(task8)
            list3.add(task9)
            list3.add(task10)
            list3.add(task11)
            list3.add(task12)
            list3.add(task13)

            return Stream.of(
                    Arguments.of(list1, 65),
                    Arguments.of(list2, 70),
                    Arguments.of(list3, 200)
            )
        }
    }
}