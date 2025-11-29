package com.hellblazer.primeMover.desmoj.dist;

/**
 * Triangular distribution with min, mode (peak), and max.
 */
public class Triangular extends ContinuousDistribution {
    private final double min;
    private final double mode;
    private final double max;
    
    public Triangular(double min, double mode, double max) {
        super();
        if (min >= mode || mode >= max) 
            throw new IllegalArgumentException("Must have min < mode < max");
        this.min = min;
        this.mode = mode;
        this.max = max;
    }
    
    public Triangular(double min, double mode, double max, long seed) {
        super(seed);
        if (min >= mode || mode >= max) 
            throw new IllegalArgumentException("Must have min < mode < max");
        this.min = min;
        this.mode = mode;
        this.max = max;
    }
    
    @Override
    public Double sample() {
        var u = rng.nextDouble();
        var fc = (mode - min) / (max - min);
        if (u < fc) {
            return min + Math.sqrt(u * (max - min) * (mode - min));
        } else {
            return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
        }
    }
    
    public double min() { return min; }
    public double mode() { return mode; }
    public double max() { return max; }
}
