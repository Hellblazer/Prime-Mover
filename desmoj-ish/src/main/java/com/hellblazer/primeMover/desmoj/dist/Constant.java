package com.hellblazer.primeMover.desmoj.dist;

/**
 * Constant "distribution" - always returns the same value.
 * Useful for testing or deterministic scenarios.
 */
public class Constant extends ContinuousDistribution {
    private final double value;
    
    public Constant(double value) {
        super();
        this.value = value;
    }
    
    @Override
    public Double sample() {
        return value;
    }
    
    public double value() { return value; }
}
