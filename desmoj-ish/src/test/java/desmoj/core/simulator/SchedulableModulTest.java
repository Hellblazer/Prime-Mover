package desmoj.core.simulator;

import desmoj.implementation.TestModel;
import desmoj.implementation.TestSchedulable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This class tests if a schedulable object is scheduled or not.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public abstract class SchedulableModulTest {

    TestModel       model;
    TestSchedulable schedul;

    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);
        this.schedul = new TestSchedulable(model, "First Test Schedulable", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsScheduled() {
        assertFalse(schedul.isScheduled());

    }

}
