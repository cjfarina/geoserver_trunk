package org.geogit.repository;

public class Tuple<M, N> {

    private M first;

    private N last;

    public Tuple(M first, N last) {
        this.first = first;
        this.last = last;
    }

    public M getFirst() {
        return first;
    }

    public N getLast() {
        return last;
    }
}
