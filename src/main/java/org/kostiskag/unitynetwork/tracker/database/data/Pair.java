package org.kostiskag.unitynetwork.tracker.database.data;

public class Pair<A, B> {
    A val1;
    B val2;

    public Pair(A val1, B val2) {
        this.val1 = val1;
        this.val2 = val2;
    }

    public A getVal1() {
        return val1;
    }

    public B getVal2() {
        return val2;
    }
}
