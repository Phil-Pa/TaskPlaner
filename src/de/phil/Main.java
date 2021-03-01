package de.phil;

import java.time.Duration;
import java.util.List;

class Wrapper {
    private int value;

    public Wrapper(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

public class Main {

    private static void foo(List list, Wrapper sum) {
        if (list.size() > 0) {
            try {
                List subList = (List)list.get(0);
                foo(subList, sum);
            } catch (Exception e) {
                int value = 0;

                for (Object o : list) {
                    value += (int) o;
                }

                sum.setValue(sum.getValue() + value);
                System.out.println(value);
            }
        }
    }

    public static void main(String[] args) {

//        Map<Integer, Object> map = new HashMap<>();
//
//        List<List<Integer>> a = List.of(List.of(1));
//        List<List<List<Integer>>> b = List.of(List.of(List.of(2, 5)));
//        List<List<List<List<Integer>>>> c = List.of(List.of(List.of(List.of(3, 4))));
//
//        map.put(1, a);
//        map.put(2, b);
//        map.put(3, c);
//
//        Wrapper sum = new Wrapper(0);
//
//        for (var entry : map.entrySet()) {
//            foo((List)entry.getValue(), sum);
//        }
//
//        System.out.println(sum.getValue());
//
//        int[] array = new int[] { 1, 2, 4 };
//
//        System.out.println(Arrays.stream(array).sum());

        /*
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
        var t11 = new Task(11, "", Duration.ofMinutes(10), false, List.of(10));
        var t12 = new Task(12, "", Duration.ofMinutes(20), true, List.of(10));
        var t13 = new Task(13, "", Duration.ofMinutes(30), false, List.of(10));
        var t14 = new Task(14, "", Duration.ofMinutes(20), false, List.of(11, 12, 13));

        Scheduler scheduler = new Scheduler(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
        var result = scheduler.scheduleTasks();
         */

        bar();
    }

    private static void bar() {
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
        System.out.println(result.getTotalDuration().toMinutes());
    }

}
