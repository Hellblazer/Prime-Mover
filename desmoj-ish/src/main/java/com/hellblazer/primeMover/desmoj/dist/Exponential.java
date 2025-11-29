package com.hellblazer.primeMover.desmoj.dist;

/**
 * Exponential distribution with given mean.
 */
public class Exponential extends ContinuousDistribution {
    private final double mean;
    
    public Exponential(double mean) {
        super();
        if (mean <= 0) throw new IllegalArgumentException("Mean must be positive");
        this.mean = mean;
    }
    
    public Exponential(double mean, long seed) {
        super(seed);
        if (mean <= 0) throw new IllegalArgumentException("Mean must be positive");
        this.mean = mean;
    }
    
    @Override
    public Double sample() {
        return rng.nextExponential() * mean;
    }
    
    public double mean() { return mean; }
}
