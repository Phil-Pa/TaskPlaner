package de.phil;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

class Permutations<E> implements  Iterator<List<E>>{

    private final List<E> arr;
    private final int[] ind;
    private boolean has_next;

    private final List<E> output;//next() returns this array, make it public

    Permutations(List<E> arr){
        this.arr = new ArrayList<>(arr);
        ind = new int[arr.size()];
        //convert an array of any elements into array of integers - first occurrence is used to enumerate
        Map<E, Integer> hm = new HashMap<E, Integer>();
        for(int i = 0; i < arr.size(); i++){
            Integer n = hm.get(arr.get(i));
            if (n == null){
                hm.put(arr.get(i), i);
                n = i;
            }
            ind[i] = n;
        }
        Arrays.sort(ind);//start with ascending sequence of integers


        //output = new E[arr.length]; <-- cannot do in Java with generics, so use reflection
        output = new ArrayList<>(arr);
        has_next = true;
    }

    public boolean hasNext() {
        return has_next;
    }

    public List<E> next() {
        if (!has_next)
            throw new NoSuchElementException();

        for(int i = 0; i < ind.length; i++){
            output.set(i, arr.get(ind[i]));
        }


        //get next permutation
        has_next = false;
        for(int tail = ind.length - 1;tail > 0;tail--){
            if (ind[tail - 1] < ind[tail]){//still increasing

                //find last element which does not exceed ind[tail-1]
                int s = ind.length - 1;
                while(ind[tail-1] >= ind[s])
                    s--;

                swap(ind, tail-1, s);

                //reverse order of elements in the tail
                for(int i = tail, j = ind.length - 1; i < j; i++, j--){
                    swap(ind, i, j);
                }
                has_next = true;
                break;
            }

        }
        return output;
    }

    private void swap(int[] arr, int i, int j){
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public void remove() {

    }
}

public class Scheduler {

    public ScheduleResult scheduleTasks(List<Task> tasks) {

        List<ScheduleResult> results = new ArrayList<>();

        List<List<Task>> permutations = buildTaskPermutations(tasks);
        for (List<Task> list : permutations) {
            TaskRunSimulator simulator = new TaskRunSimulator(list);
            ScheduleResult result = simulator.run();
            if (result != null)
                results.add(result);
        }

        // if there are multiple best duration, sort for the duration with the highest wait time
        // and then for length of the intervals (higher is better)

        Duration bestDuration = Duration.ofDays(1);
        int bestDurationIndex = 0;

        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getTotalDuration().minus(bestDuration).isNegative()) {
                bestDuration = results.get(i).getTotalDuration();
                bestDurationIndex = i;
            }
        }

        return results.get(bestDurationIndex);
    }

    private static List<List<Task>> buildTaskPermutations(List<Task> tasks) {
        // init tasks done ids
        List<Integer> tasksDoneIds = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++)
            tasksDoneIds.add(-1);

        List<List<Task>> lists = new ArrayList<>();
        Permutations<Task> permutations = new Permutations<>(tasks);

        int count = 0;

        while (permutations.hasNext()) {
            List<Task> perm = permutations.next();

            Collections.fill(tasksDoneIds, -1);

//            if (count == 0 || count == 24 || count == 120 || count == 720 || count == 744 || count == 960)
//                System.out.println();

            if (isDoable(perm, tasksDoneIds)) {
//                System.out.println("added at " + count);
                lists.add(new ArrayList<>(perm));
            }

            count++;
        }

        System.out.printf("%d/%d valid permutations\n", lists.size(), count);

        return lists;
    }

    private static boolean isDoable(List<Task> perm, List<Integer> taskIdsDone) {

        int currentTaskNum = 0;

        for (Task task : perm) {
            // if dependent tasks are done
            if (task.hasDependentTasks()) {

                if (currentTaskNum == 0)
                    return false;

                for (int id : task.getDependentTaskIds())  {
                    if (!taskIdsDone.contains(id))
                        return false;
                }
            }

            taskIdsDone.set(currentTaskNum++, task.getId());
        }

        return currentTaskNum == taskIdsDone.size();
    }

}
