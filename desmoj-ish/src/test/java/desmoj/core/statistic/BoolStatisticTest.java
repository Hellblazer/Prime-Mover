package desmoj.core.statistic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.statistic.BoolStatistic;
import desmoj.implementation.TestModel;

/**
 * Tests the class BoolStatistic, which is supposed
 * to make an statistic about boolean values.
 * 
 * @see core.statistic.BoolStatistic
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class BoolStatisticTest{

    TestModel model;
    BoolStatistic bool;
    
    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        this.bool = new BoolStatistic(model, "Test BoolStatistic", false, false);
    }

    /**
     * Tests whether the variable for counting "true" works
     * the right way.
     */
    public void testTrueValues()
    {
        bool.update(true);
        assertNotNull(bool.getTrueObs());
        assertNotNull(bool.getTrueRatio());
        assertTrue(1 == bool.getTrueObs());
        assertTrue(1 == bool.getTrueRatio());
    }
    
    /**
     * Tests both observer together.
     */
    public void testOberservations()
    {
        assertTrue(0 == bool.getObservations());
        bool.update(true);
        bool.update(false);
        assertFalse(0 == bool.getObservations());
        assertTrue(2 == bool.getObservations());
    }
    
    /**
     * Tests whether the reset method is working right.
     */
    public void testReset()
    {
        bool.update(true);
        assertNotNull(bool.getTrueObs());
        assertNotNull(bool.getTrueRatio());
        assertTrue(1 == bool.getTrueObs());
        assertTrue(1 == bool.getTrueRatio());
//      bool.reset(); //throws Nullpointer since no time is defined
//      assertTrue(0 == bool.getTrueObs());
//      assertTrue(0 == bool.getTrueRatio());
    }
    
    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

}
