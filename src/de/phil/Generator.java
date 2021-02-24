package de.phil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

interface Bar<T> {
    boolean hasNext();
    T produce();
}

public class Generator<T> {

    private final Consumer<T> consumer;
    private final Bar<T> predicate;

    public Generator(Consumer<T> consumer, Bar<T> predicate) {
        this.consumer = consumer;
        this.predicate = predicate;
    }

    public void generate() {
        while (predicate.hasNext()) {
            consumer.accept(predicate.produce());
        }
    }

}
