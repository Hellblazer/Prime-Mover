package com.hellblazer.primeMover.desmoj.dist;

/**
 * Normal (Gaussian) distribution.
 */
public class Normal extends ContinuousDistribution {
    private final double mean;
    private final double stdDev;
    
    public Normal(double mean, double stdDev) {
        super();
        if (stdDev < 0) throw new IllegalArgumentException("StdDev must be non-negative");
        this.mean = mean;
        this.stdDev = stdDev;
    }
    
    public Normal(double mean, double stdDev, long seed) {
        super(seed);
        if (stdDev < 0) throw new IllegalArgumentException("StdDev must be non-negative");
        this.mean = mean;
        this.stdDev = stdDev;
    }
    
    @Override
    public Double sample() {
        return rng.nextGaussian(mean, stdDev);
    }
    
    public double mean() { return mean; }
    public double stdDev() { return stdDev; }
}
