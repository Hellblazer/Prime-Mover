package com.hellblazer.primeMover.desmoj.dist;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Base class for continuous distributions returning Double values.
 */
public abstract class ContinuousDistribution implements Distribution<Double> {
    protected RandomGenerator rng;
    protected long seed;
    
    protected ContinuousDistribution(long seed) {
        this.seed = seed;
        this.rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }
    
    protected ContinuousDistribution() {
        this(System.nanoTime());
    }
    
    @Override
    public void reset() {
        this.rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }
    
    @Override
    public void reset(long newSeed) {
        this.seed = newSeed;
        reset();
    }
    
    /**
     * Sample as duration (long) - useful for simulation time.
     * Multiplies sample by scale factor and rounds.
     */
    public long sampleDuration(double scale) {
        return Math.round(sample() * scale);
    }
    
    /**
     * Sample as duration (long) - assumes sample is already in time units.
     */
    public long sampleDuration() {
        return Math.round(sample());
    }
    
    // Factory methods
    public static Exponential exponential(double mean) {
        return new Exponential(mean);
    }
    
    public static Exponential exponential(double mean, long seed) {
        return new Exponential(mean, seed);
    }
    
    public static Normal normal(double mean, double stdDev) {
        return new Normal(mean, stdDev);
    }
    
    public static Normal normal(double mean, double stdDev, long seed) {
        return new Normal(mean, stdDev, seed);
    }
    
    public static Uniform uniform(double min, double max) {
        return new Uniform(min, max);
    }
    
    public static Uniform uniform(double min, double max, long seed) {
        return new Uniform(min, max, seed);
    }
    
    public static Triangular triangular(double min, double mode, double max) {
        return new Triangular(min, mode, max);
    }
    
    public static Triangular triangular(double min, double mode, double max, long seed) {
        return new Triangular(min, mode, max, seed);
    }
    
    public static Constant constant(double value) {
        return new Constant(value);
    }
}
