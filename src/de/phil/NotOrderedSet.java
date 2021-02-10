package de.phil;

import java.util.*;

public class NotOrderedSet<T> implements Set<T> {

    private final List<T> list = new ArrayList<>();

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) list.toArray();
    }

    @Override
    public boolean add(T a) {
        if (list.contains(a))
            return false;

        return list.add(a);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = true;
        for (T t : c) {
            if (list.contains(t))
                result = false;
            else
                list.add(t);
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }
}
