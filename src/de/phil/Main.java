package de.phil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Integer, Object> map = new HashMap<>();

        List<List<Integer>> a = List.of(List.of(1));
        List<List<List<Integer>>> b = List.of(List.of(List.of(2, 5)));
        List<List<List<List<Integer>>>> c = List.of(List.of(List.of(List.of(3, 4))));

        map.put(1, a);
        map.put(2, b);
        map.put(3, c);

        Wrapper sum = new Wrapper(0);

        for (var entry : map.entrySet()) {
            foo((List)entry.getValue(), sum);
        }

        System.out.println(sum.getValue());

    }

}
