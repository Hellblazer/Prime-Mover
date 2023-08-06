package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.implementation.TestEventAbstract;
import desmoj.implementation.TestModel;

/**
 * This class tests the methods derived of the abstract class Event.
 *
 * @author Sascha Winde, Clara Bluemm
 *
 */
public abstract class EventModulTest {

    TestEventAbstract event;
    TestModel         model;

    /**
     * Sets up the test fixture.
     */
    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.event = new TestEventAbstract(model, "First Test Event", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Test checks, if an event is not scheduled.
     */
    @Test
    public void testIsNotScheduled() {
        assertFalse(event.isScheduled());
    }

    /**
     * This test checks the given name.
     */
    @Test
    public void testName() {
        assertEquals("First Test Event#1", event.getName());
    }

    /**
     * This Test checks the actual RealTimeConstraint
     */
    @Test
    public void testRealTimeConstraint() {
        assertTrue(10 == event.getRealTimeConstraint());
    }

}
