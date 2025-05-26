package desmoj.core.statistic;

import desmoj.implementation.TestModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the class Aggregate. It is supposed to
 *
 * @author Sascha Winde, Clara Bluemm
 * @see core.statistic.Aggregate
 */
public class AggregateTest {

    Aggregate agg;
    TestModel model;

    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        this.agg = new Aggregate(model, "Test Aggregate", false, false);

    }

    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Tests the method getMaximum, which is supposed to return the highest value of the count object so far.
     */
    @Test
    public void testMax() {
        assertTrue(0 == agg.getMaximum());
        agg.update(10);
        assertFalse(0 == agg.getMaximum());
        assertTrue(10 == agg.getMaximum());
        agg.update(5);
        assertFalse(10 == agg.getMaximum());
        assertTrue(15 == agg.getMaximum());
    }

    /**
     * Tests the method getMinimum, which is supposed to return the lowest value of the count object so far.
     */
    @Test
    public void testMin() {
        assertTrue(0 == agg.getMinimum());
        agg.update(10);
        assertFalse(10 == agg.getMinimum());
        assertTrue(0 == agg.getMinimum());
    }

    /**
     * Tests whether the value is updated the right way.
     */
    @Test
    public void testValue() {
        assertTrue(0 == agg.getValue());
        agg.update(10);
        assertFalse(0 == agg.getValue());
        assertTrue(10 == agg.getValue());
    }

}
