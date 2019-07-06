package org.kostiskag.unitynetwork.tracker.database.data;

public class Tuple<A, B, C> {
    A val1;
    B val2;
    C val3;

    public Tuple(A val1, B val2, C val3) {
        this.val1 = val1;
        this.val2 = val2;
        this.val3 = val3;
    }

    public A getVal1() {
        return val1;
    }

    public B getVal2() {
        return val2;
    }

    public C getVal3() {
        return val3;
    }
}
