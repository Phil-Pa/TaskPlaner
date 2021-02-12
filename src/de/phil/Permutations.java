package de.phil;

import java.util.*;

public class Permutations<E> implements Iterator<List<E>> {

    private final List<E> list;
    private final int[] indices;
    private boolean hasNext;

    private final List<E> output;//next() returns this array, make it public

    Permutations(List<E> arr) {
        this.list = new ArrayList<>(arr);
        indices = new int[arr.size()];
        //convert an array of any elements into array of integers - first occurrence is used to enumerate
        Map<E, Integer> map = new HashMap<E, Integer>();
        for (int i = 0; i < arr.size(); i++) {
            Integer n = map.get(arr.get(i));
            if (n == null) {
                map.put(arr.get(i), i);
                n = i;
            }
            indices[i] = n;
        }
        Arrays.sort(indices);//start with ascending sequence of integers


        //output = new E[arr.length]; <-- cannot do in Java with generics, so use reflection
        output = new ArrayList<>(arr);
        hasNext = true;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public List<E> next() {
        if (!hasNext)
            throw new NoSuchElementException();

        for (int i = 0; i < indices.length; i++) {
            output.set(i, list.get(indices[i]));
        }


        //get next permutation
        hasNext = false;
        for (int tail = indices.length - 1; tail > 0; tail--) {
            if (indices[tail - 1] < indices[tail]) {//still increasing

                //find last element which does not exceed ind[tail-1]
                int s = indices.length - 1;
                while (indices[tail - 1] >= indices[s])
                    s--;

                swap(indices, tail - 1, s);

                //reverse order of elements in the tail
                for (int i = tail, j = indices.length - 1; i < j; i++, j--) {
                    swap(indices, i, j);
                }
                hasNext = true;
                break;
            }

        }
        return output;
    }

    private void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }
}