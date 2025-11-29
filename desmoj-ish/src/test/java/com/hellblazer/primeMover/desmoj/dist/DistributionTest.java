package com.hellblazer.primeMover.desmoj.dist;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DistributionTest {
    
    @Test
    void testExponentialMean() {
        var dist = ContinuousDistribution.exponential(100.0, 12345L);
        double sum = 0;
        int n = 10000;
        for (int i = 0; i < n; i++) {
            sum += dist.sample();
        }
        var mean = sum / n;
        // Within 5% of expected mean
        assertEquals(100.0, mean, 5.0);
    }
    
    @Test
    void testNormalMeanAndStdDev() {
        var dist = ContinuousDistribution.normal(50.0, 10.0, 12345L);
        double sum = 0;
        double sumSq = 0;
        int n = 10000;
        for (int i = 0; i < n; i++) {
            var sample = dist.sample();
            sum += sample;
            sumSq += sample * sample;
        }
        var mean = sum / n;
        var variance = (sumSq / n) - (mean * mean);
        var stdDev = Math.sqrt(variance);
        
        assertEquals(50.0, mean, 1.0);
        assertEquals(10.0, stdDev, 1.0);
    }
    
    @Test
    void testUniformRange() {
        var dist = ContinuousDistribution.uniform(10.0, 20.0, 12345L);
        for (int i = 0; i < 1000; i++) {
            var sample = dist.sample();
            assertTrue(sample >= 10.0 && sample < 20.0);
        }
    }
    
    @Test
    void testTriangularRange() {
        var dist = ContinuousDistribution.triangular(0.0, 5.0, 10.0, 12345L);
        for (int i = 0; i < 1000; i++) {
            var sample = dist.sample();
            assertTrue(sample >= 0.0 && sample <= 10.0);
        }
    }
    
    @Test
    void testConstant() {
        var dist = ContinuousDistribution.constant(42.0);
        for (int i = 0; i < 100; i++) {
            assertEquals(42.0, dist.sample());
        }
    }
    
    @Test
    void testSampleDuration() {
        var dist = ContinuousDistribution.constant(10.5);
        assertEquals(11, dist.sampleDuration());  // Rounded
        assertEquals(105, dist.sampleDuration(10.0));  // Scaled
    }
    
    @Test
    void testReset() {
        var dist = ContinuousDistribution.exponential(100.0, 12345L);
        var samples1 = new double[10];
        for (int i = 0; i < 10; i++) {
            samples1[i] = dist.sample();
        }
        
        dist.reset();
        
        for (int i = 0; i < 10; i++) {
            assertEquals(samples1[i], dist.sample());
        }
    }
    
    @Test
    void testResetWithNewSeed() {
        var dist = ContinuousDistribution.exponential(100.0, 12345L);
        var sample1 = dist.sample();
        
        dist.reset(54321L);
        var sample2 = dist.sample();
        
        assertNotEquals(sample1, sample2);
    }
    
    @Test
    void testExponentialInvalidMean() {
        assertThrows(IllegalArgumentException.class, () -> 
            ContinuousDistribution.exponential(-1.0));
        assertThrows(IllegalArgumentException.class, () -> 
            ContinuousDistribution.exponential(0.0));
    }
    
    @Test
    void testUniformInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> 
            ContinuousDistribution.uniform(10.0, 5.0));
        assertThrows(IllegalArgumentException.class, () -> 
            ContinuousDistribution.uniform(5.0, 5.0));
    }
}
