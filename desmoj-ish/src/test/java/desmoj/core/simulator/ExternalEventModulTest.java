package desmoj.core.simulator;

import desmoj.implementation.TestExternalEvent;
import desmoj.implementation.TestModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This Class simply tests if an Event is external or not. Further functionality is tested in the EventModulTest Class.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public abstract class ExternalEventModulTest {

    TestExternalEvent externalEvent;
    TestModel         model;

    @BeforeEach
    public void setUp() throws Exception {

        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);
        this.externalEvent = new TestExternalEvent(model, "First Test ExternalEvent", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This checks if the event is external or not.
     */
    @Test
    public void testIsExternal() {
        assertTrue(externalEvent.isExternal());
    }

    /**
     * This tests checks the given name.
     */
    @Test
    public void testName() {
        assertEquals("First Test ExternalEvent#1", externalEvent.getName());
    }

}
