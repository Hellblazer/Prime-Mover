package com.hellblazer.primeMover.desmoj.dist;

import java.util.random.RandomGenerator;

/**
 * Base interface for distributions. Modern, simple API.
 */
public interface Distribution<T> {
    /**
     * Sample a value from the distribution.
     */
    T sample();
    
    /**
     * Reset the distribution (re-seed with same seed).
     */
    void reset();
    
    /**
     * Reset with a new seed.
     */
    void reset(long seed);
}
