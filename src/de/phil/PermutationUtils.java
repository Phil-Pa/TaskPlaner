package de.phil;

import java.util.List;
import java.util.stream.Collectors;

public class PermutationUtils {

    // util function for debugging and setting break points
    // if (PermutationUtils.equals(list, new int[]{2, 5, 6, 1, 7, 9, 11, 3, 4, 8, 10, 12, 13}))
    static boolean equals(List<Task> tasks, int[] ids) {
        List<Integer> idList = tasks.stream().map(Task::getId).collect(Collectors.toList());
        assert(idList.size() == ids.length);

        for (int i = 0; i < ids.length; i++) {
            if (idList.get(i) != ids[i]) {
                return false;
            }
        }
        return true;
    }

}
