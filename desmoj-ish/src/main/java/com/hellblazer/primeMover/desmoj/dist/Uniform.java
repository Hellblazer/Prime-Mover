package com.hellblazer.primeMover.desmoj.dist;

/**
 * Uniform distribution between min and max.
 */
public class Uniform extends ContinuousDistribution {
    private final double min;
    private final double max;
    
    public Uniform(double min, double max) {
        super();
        if (min >= max) throw new IllegalArgumentException("min must be less than max");
        this.min = min;
        this.max = max;
    }
    
    public Uniform(double min, double max, long seed) {
        super(seed);
        if (min >= max) throw new IllegalArgumentException("min must be less than max");
        this.min = min;
        this.max = max;
    }
    
    @Override
    public Double sample() {
        return rng.nextDouble(min, max);
    }
    
    public double min() { return min; }
    public double max() { return max; }
}
