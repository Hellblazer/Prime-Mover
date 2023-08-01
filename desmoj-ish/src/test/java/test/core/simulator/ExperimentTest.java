package test.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import test.implementation.TestModel;

/**
 * This class tests the experiment Class on it's own. Especially the
 * functionality to set values to model relevant instances.
 * 
 * @author Sascha
 *
 */
public class ExperimentTest {

    Experiment experiment;
    TestModel  model;

    @BeforeEach
    public void setUp() throws Exception {

        this.model = new TestModel();
        this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                         java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * The Debugmode is tested. Therefore it is tested off, and tested after being
     * set on.
     */
    public void testDebug() {
        assertFalse(experiment.debugIsOn());
        experiment.debugOn(new TimeInstant(0));
        experiment.start();
        assertTrue(experiment.debugIsOn());
    }

    /**
     * Tests the Delay in milliseconds of the experiment.
     */
    public void testDelay() {
        assertTrue(0 == experiment.getDelayInMillis());
        experiment.setDelayInMillis(10);
        assertTrue(10 == experiment.getDelayInMillis());
    }

    /**
     * This Test checks the ExecutionSpeedRate
     */
    public void testExecutionSpeedRate() {
        assertTrue(0 == experiment.getExecutionSpeedRate());
        experiment.setExecutionSpeedRate(10);
        assertTrue(10 == experiment.getExecutionSpeedRate());
    }

    /**
     * this test checks if the experiment is connected to a model.
     */
    public void testModel() {
        assertEquals(model, experiment.getModel());
        assertTrue(experiment.isConnected());
    }

    /**
     * this test checks if the experiment randomizes Events.
     */
    public void testRandomizingEvents() {
        assertFalse(experiment.isRandomizingConcurrentEvents());
        experiment.randomizeConcurrentEvents(true);
        assertTrue(experiment.isRandomizingConcurrentEvents());
    }

    /**
     * this test checks if the experiment is connected to a ResourceDB.
     */
    public void testResourceDB() {
        assertNotNull(experiment.getResourceDB());
    }

    /**
     * this test checks if the experiment is connected to a scheduler.
     */
    public void testScheduler() {
        assertNotNull(experiment.getScheduler());
    }

    /**
     * this test checks if the experiment is connected to a SimClock.
     */
    public void testSimClock() {
        assertNotNull(experiment.getSimClock());
    }

    /**
     * This Test checks the start and stop function of the experiment. Starttime has
     * to be 0 and stoptime is 100.
     * 
     * @throws InterruptedException
     */
    public void testStartStopExperiment() throws InterruptedException {
        assertTrue(0 == experiment.STARTED);
        experiment.stop(new TimeInstant(100));
        experiment.start();
        assertTrue(100 == experiment.getStopTime().getTimeAsDouble());
        assertTrue(1 == experiment.STOPPED);
    }

}
