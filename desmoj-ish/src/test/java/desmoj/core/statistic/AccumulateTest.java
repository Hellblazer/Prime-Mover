package desmoj.core.statistic;

import desmoj.core.simulator.Experiment;
import desmoj.implementation.TestModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the class Accumulate
 *
 * @author Sascha Winde, Clara Bluemm
 * @see core.statistics.Accumulate
 */
public class AccumulateTest {

    TestModel  model;
    Accumulate testAcc;

    /**
     * Sets up the testfixture before every test.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.testAcc = new Accumulate(model, "First Test Accumulate", false, false);

    }

    /**
     * Destroys the testfixture after every test.
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Tests whether the reporter can be turned on and off in the right way.
     */
    @Test
    public void testReport() {
        assertFalse(testAcc.reportIsOn());
        testAcc.reportOn();
        assertTrue(testAcc.reportIsOn());
        testAcc.reportOff();
        assertFalse(testAcc.reportIsOn());
    }

    /**
     * Tests whether a reporter can be created to the object.
     */
    @Test
    public void testReporter() {
        assertNotNull(testAcc.createDefaultReporter());
    }

    /**
     * Tests whether the flag is set in the right way.
     */
    @Test
    public void testRetainLastValueOnReset() {
        testAcc.setRetainLastValueOnReset(false);
        assertFalse(testAcc.doesRetainLastValueOnReset());
        testAcc.setRetainLastValueOnReset(true);
        assertTrue(testAcc.doesRetainLastValueOnReset());
    }

}
