package de.phil;

import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SchedulerTest {

    private void testOneTaskImpl(Task task) {

        Scheduler scheduler = new Scheduler(task);
        var result = scheduler.scheduleTasks();

        assertEquals(Duration.ofMinutes(10), result.getTotalDuration());
//        assertEquals(1, result.getOrderedTasksIds().size());
//        assertEquals(1, result.getOrderedTasksIds().get(0).intValue());
//        assertFalse(result.hasMultipleResults());

    }

    @Test
    public void testOneTask() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, null);
        testOneTaskImpl(t1);

        var t2 = new Task(1, "", Duration.ofMinutes(10), true, null);
        testOneTaskImpl(t2);
    }

    @Test
    public void testTwoTasks() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, null);
        var t2 = new Task(2, "", Duration.ofMinutes(10), false, null);

        Scheduler scheduler = new Scheduler(t1, t2);
        var result = scheduler.scheduleTasks();
        assertEquals(Duration.ofMinutes(20), result.getTotalDuration());
    }

    @Test
    public void testThrowIfDependentTaskIdListIsEmpty() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, new ArrayList<>());

        try {
            Scheduler scheduler = new Scheduler(t1);
            fail("dependent task id list must not be empty");
        } catch (AssertionError ignored) {

        }
    }

    @Test
    public void testSmall() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), true, null);
        var t2 = new Task(2, "", Duration.ofMinutes(20), true, null);
        var t6 = new Task(3, "", Duration.ofMinutes(20), true, List.of(1));
        var t7 = new Task(4, "", Duration.ofMinutes(5), true, List.of(2));
        var t8 = new Task(5, "", Duration.ofMinutes(5), true, List.of(3, 4));
        var t9 = new Task(6, "", Duration.ofMinutes(5), false, List.of(5));
        var t10 = new Task(7, "", Duration.ofMinutes(5), false, List.of(6));

        Scheduler scheduler = new Scheduler(t1, t2, t6, t7, t8, t9, t10);
        ScheduleResult result = scheduler.scheduleTasks();

        assertEquals(Duration.ofMinutes(45), result.getTotalDuration());
    }

    @Test
    public void testParallel() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), true, null);
        var t2 = new Task(2, "", Duration.ofMinutes(20), true, null);
        var t3 = new Task(3, "", Duration.ofMinutes(5), true, null);
        var t4 = new Task(4, "", Duration.ofMinutes(10), true, null);
        var t5 = new Task(5, "", Duration.ofMinutes(10), true, null);

        var t6 = new Task(6, "", Duration.ofMinutes(20), true, List.of(1));
        var t7 = new Task(7, "", Duration.ofMinutes(5), true, List.of(2));
        var t8 = new Task(8, "", Duration.ofMinutes(50), true, List.of(3, 4));
        var t9 = new Task(9, "", Duration.ofMinutes(20), true, List.of(5));

        var t10 = new Task(10, "", Duration.ofMinutes(10), false, List.of(6, 7, 8, 9));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        var result = scheduler.scheduleTasks();

//        assertTrue(result.hasMultipleResults());

//        assertEquals(1, result.getWaitIntervals().size());
//        assertTrue(result.getWaitIntervals().containsKey(Duration.ofMinutes(70)));
//        assertTrue(result.getWaitIntervals().containsValue(1));

        assertEquals(Duration.ofMinutes(70), result.getTotalDuration());
    }

    @Test
    public void testSequential() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, null);
        var t2 = new Task(2, "", Duration.ofMinutes(20), false, null);
        var t3 = new Task(3, "", Duration.ofMinutes(5), false, null);
        var t4 = new Task(4, "", Duration.ofMinutes(10), false, null);
        var t5 = new Task(5, "", Duration.ofMinutes(10), false, null);

        var t6 = new Task(6, "", Duration.ofMinutes(20), false, List.of(1));
        var t7 = new Task(7, "", Duration.ofMinutes(5), false, List.of(2));
        var t8 = new Task(8, "", Duration.ofMinutes(50), false, List.of(3, 4));
        var t9 = new Task(9, "", Duration.ofMinutes(20), false, List.of(5));

        var t10 = new Task(10, "", Duration.ofMinutes(30), false, List.of(6, 7));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        var result = scheduler.scheduleTasks();

//        assertTrue(result.hasMultipleResults());
//        assertTrue(result.getWaitIntervals().isEmpty());
        assertEquals(Duration.ofMinutes(180), result.getTotalDuration());
    }

    @Test
    public void testMixedSmall() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), true, null);
        var t2 = new Task(2, "", Duration.ofMinutes(20), true, null);
        var t3 = new Task(3, "", Duration.ofMinutes(20), false, null);
        var t4 = new Task(4, "", Duration.ofMinutes(10), false, List.of(1));
        var t5 = new Task(5, "", Duration.ofMinutes(50), false, List.of(2));
        var t6 = new Task(6, "", Duration.ofMinutes(80), true, List.of(3, 5));
        var t7 = new Task(7, "", Duration.ofMinutes(100), true, List.of(4));
        var t8 = new Task(8, "", Duration.ofMinutes(10), false, List.of(6, 7));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8);
        var result = scheduler.scheduleTasks();

        assertEquals(170, result.getTotalDuration().toMinutes());
        assertEquals(1, result.getWaitIntervals().size());
        assertTrue(result.getWaitIntervals().containsKey(Duration.ofMinutes(80)));
        assertEquals(1, result.getWaitIntervals().get(Duration.ofMinutes(80)).intValue());
        assertEquals(List.of(2, 1, 3, 4, 7, 5, 6, 8), result.getOrderedTasksIds());
    }

    // TODO: rewrite @Test as tree data structure
    public void testMixedMiddle() {
        var t1 = new Task(1, "", Duration.ofMinutes(5), false, null);
        var t2 = new Task(2, "", Duration.ofMinutes(10), false, List.of(1));
        var t3 = new Task(3, "", Duration.ofMinutes(20), false, List.of(2));
        var t4 = new Task(4, "", Duration.ofMinutes(100), true, List.of(3));
        var t5 = new Task(5, "", Duration.ofMinutes(300), false, List.of(4));
        var t6 = new Task(6, "", Duration.ofMinutes(10), true, null);
        var t7 = new Task(7, "", Duration.ofMinutes(10), false, List.of(6));
        var t8 = new Task(8, "", Duration.ofMinutes(200), false, List.of(7));
        var t9 = new Task(9, "", Duration.ofMinutes(1000), true, List.of(8));
        var t10 = new Task(10, "", Duration.ofMinutes(10), false, List.of(8, 9));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
        var result = scheduler.scheduleTasks();

        assertEquals(Duration.ofMinutes(1230), result.getTotalDuration());
    }

    @Test
    public void testMixedBig() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, null);
        var t2 = new Task(2, "", Duration.ofMinutes(10), true, null);
        var t3 = new Task(3, "", Duration.ofMinutes(5), false, null);
        var t4 = new Task(4, "", Duration.ofMinutes(10), false, null);
        var t5 = new Task(5, "", Duration.ofMinutes(20), true, null);
        var t6 = new Task(6, "", Duration.ofMinutes(10), false, null);
        var t7 = new Task(7, "", Duration.ofMinutes(10), true, List.of(1));
        var t8 = new Task(8, "", Duration.ofMinutes(10), false, List.of(2, 3, 4));
        var t9 = new Task(9, "", Duration.ofMinutes(30), false, List.of(5, 6));
        var t10 = new Task(10, "", Duration.ofMinutes(5), true, List.of(7, 8));
        var t11 = new Task(11, "", Duration.ofMinutes(50), false, List.of(9));
        var t12 = new Task(12, "", Duration.ofMinutes(10), false, List.of(11));
        var t13 = new Task(13, "", Duration.ofMinutes(20), false, List.of(10, 12));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
        var result = scheduler.scheduleTasks();

        System.out.println(Arrays.toString(result.getOrderedTasksIds().toArray()));
        // TODO: 155 maybe false?
        assertEquals(Duration.ofMinutes(155), result.getTotalDuration());
    }

    // TODO: rewrite test as tree data structure @Test
    public void testMixed() {
        var t1 = new Task(1, "", Duration.ofMinutes(10), false, null);
        var t2 = new Task(2, "", Duration.ofMinutes(20), false, null);
        var t3 = new Task(3, "", Duration.ofMinutes(5), false, null);
        var t4 = new Task(4, "", Duration.ofMinutes(10), true, null);
        var t5 = new Task(5, "", Duration.ofMinutes(10), false, null);

        var t6 = new Task(6, "", Duration.ofMinutes(20), true, List.of(1));
        var t7 = new Task(7, "", Duration.ofMinutes(5), true, List.of(2));
        var t8 = new Task(8, "", Duration.ofMinutes(50), true, List.of(3, 4));
        var t9 = new Task(9, "", Duration.ofMinutes(20), true, List.of(5));

        var t10 = new Task(10, "", Duration.ofMinutes(30), false, List.of(6, 7));
        var t11 = new Task(11, "", Duration.ofMinutes(20), true, List.of(7));
        var t12 = new Task(12, "", Duration.ofMinutes(5), false, List.of(8));
        var t13 = new Task(13, "", Duration.ofMinutes(10), true, List.of(9));

        var t14 = new Task(14, "", Duration.ofMinutes(20), false, List.of(10, 11));
        var t15 = new Task(15, "", Duration.ofMinutes(10), false, List.of(14));
        var t16 = new Task(16, "", Duration.ofMinutes(10), true, List.of(12, 14));
        var t17 = new Task(17, "", Duration.ofMinutes(30), false, List.of(13));

        var t18 = new Task(18, "", Duration.ofMinutes(30), false, List.of(15));
        var t19 = new Task(19, "", Duration.ofMinutes(20), true, List.of(15, 16));
        var t20 = new Task(20, "", Duration.ofMinutes(15), true, List.of(16));
        var t21 = new Task(21, "", Duration.ofMinutes(15), true, List.of(16, 17));

        var t22 = new Task(22, "", Duration.ofMinutes(20), true, List.of(18));
        var t23 = new Task(23, "", Duration.ofMinutes(10), false, List.of(19));
        var t24 = new Task(24, "", Duration.ofMinutes(5), false, List.of(20));
        var t25 = new Task(25, "", Duration.ofMinutes(15), true, List.of(21));

        var t26 = new Task(26, "", Duration.ofMinutes(10), false, List.of(22, 23));
        var t27 = new Task(27, "", Duration.ofMinutes(5), true, List.of(23, 24));
        var t28 = new Task(28, "", Duration.ofMinutes(15), true, List.of(25));
        var t29 = new Task(29, "", Duration.ofMinutes(30), false, List.of(26, 27, 28));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13/*, t14, t15, t16, t17, t18, t19, t20, t21, t22, t23, t24, t25, t26, t27, t28, t29*/);
        var result = scheduler.scheduleTasks();

        assertEquals(Duration.ofMinutes(235), result.getTotalDuration());
        assertEquals(29, result.getOrderedTasksIds().size());
        assertEquals(29, result.getOrderedTasksIds().stream().distinct().count());
    }

    @Test
    public void testGetTaskDepth() {
        List<Task> tasks = List.of(new Task(1, null), new Task(2, List.of(1)), new Task(3, List.of(2)), new Task(4, List.of(1)));

        assertEquals(1, Scheduler.getTaskDepth(tasks, tasks.get(0)));
        assertEquals(2, Scheduler.getTaskDepth(tasks, tasks.get(1)));
        assertEquals(3, Scheduler.getTaskDepth(tasks, tasks.get(2)));
        assertEquals(2, Scheduler.getTaskDepth(tasks, tasks.get(3)));
    }

}
