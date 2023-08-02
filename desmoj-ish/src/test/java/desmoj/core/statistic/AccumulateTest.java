package desmoj.core.statistic;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.implementation.TestModel;

/**
 * Tests the class Accumulate
 *
 * @see core.statistics.Accumulate
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class AccumulateTest{

    TestModel model;
    Accumulate testAcc;

    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS, java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.testAcc = new Accumulate(model, "First Test Accumulate", false, false);

    }

    /**
     * Tests whether a reporter can be created to the object.
     */
    public void testReporter()
    {
        assertNotNull(testAcc.createDefaultReporter());
    }

    /**
     * Tests whether the flag is set in the right way.
     */
    public void testRetainLastValueOnReset()
    {
        testAcc.setRetainLastValueOnReset(false);
        assertFalse(testAcc.doesRetainLastValueOnReset());
        testAcc.setRetainLastValueOnReset(true);
        assertTrue(testAcc.doesRetainLastValueOnReset());
    }

    /**
     * Tests whether the reporter can be turned on
     * and off in the right way.
     */
    public void testReport()
    {
        assertFalse(testAcc.reportIsOn());
        testAcc.reportOn();
        assertTrue(testAcc.reportIsOn());
        testAcc.reportOff();
        assertFalse(testAcc.reportIsOn());
    }

    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

}
