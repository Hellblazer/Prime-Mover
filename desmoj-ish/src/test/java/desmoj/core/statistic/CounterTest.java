package desmoj.core.statistic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.implementation.TestModel;

/**
 * This class is the test of class count, which is used to simple count
 * something (e.g. some kind of objects) during an experiment.
 *
 * @see core.statistic.count
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class CounterTest {

    Count     count;
    TestModel model;

    /**
     * Setting up the testfixture.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        this.count = new Count(model, "Test Counter", false, false);
    }

    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Tests the method getMaximum, which is supposed to return the highest value of
     * the count object so far.
     */
    @Test
    public void testMax() {
        assertTrue(0 == count.getMaximum());
        count.update(10);
        assertFalse(0 == count.getMaximum());
        assertTrue(10 == count.getMaximum());
        count.update(5);
        assertFalse(10 == count.getMaximum());
        assertTrue(15 == count.getMaximum());
    }

    /**
     * Tests the method getMinimum, which is supposed to return the lowest value of
     * the count object so far.
     */
    @Test
    public void testMin() {
        assertTrue(0 == count.getMinimum());
        count.update(10);
        assertFalse(10 == count.getMinimum());
        assertTrue(0 == count.getMinimum());
    }

    /**
     * Tests the method getValue. It is supposed to return the actual value.
     */
    @Test
    public void testValue() {
        assertTrue(0 == count.getValue());
        count.update(10);
        assertFalse(0 == count.getValue());
        assertTrue(10 == count.getValue());
    }

}
