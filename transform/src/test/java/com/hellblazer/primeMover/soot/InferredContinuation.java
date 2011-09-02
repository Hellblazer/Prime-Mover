package com.hellblazer.primeMover.soot;

public class InferredContinuation {
    ContinuationPrototype cont;

    public void a() {
        b();
    }

    public void b() {
        cont.first();
    }

    public void setCont(ContinuationPrototype p) {
        cont = p;
    }
}
