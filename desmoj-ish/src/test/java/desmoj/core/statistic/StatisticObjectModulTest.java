package desmoj.core.statistic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.implementation.TestModel;
import desmoj.implementation.TestStatisticObject;

/**
 * Tests the class StatisticObject. It is the superclass
 * of the other statistical classes, which collect data.
 * Since it is declared as abstract this test class is
 * abstract as well. It is implemented by StatisticObjectTest
 * 
 * @see core.statistic.StatisticObject
 * @author Sascha Winde, Clara Bluemm
 *
 */
public abstract class StatisticObjectModulTest{
    
    TestModel model;
    TestStatisticObject stats;

    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        
        this.model = new TestModel();
        this.stats = new TestStatisticObject(model, "First Test Statistic Object", false, true);
        
    }

    /**
     * Tests whether the trace can be switched on.
     */
    public void testTrace()
    {
        assertTrue(stats.traceIsOn());
        stats.traceOff();
        assertFalse(stats.traceIsOn());
    }
    
    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

}
