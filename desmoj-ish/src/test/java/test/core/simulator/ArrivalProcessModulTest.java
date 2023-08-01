package test.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.SimProcess;
import test.implementation.TestArrivalProcess;
import test.implementation.TestModel;
import test.implementation.TestRealDist;

/**
 * This class tests the functionality of the class ArrivalProcess.
 * 
 * @author Sascha Winde, Clara Bluemm
 *
 * 
 */
public abstract class ArrivalProcessModulTest {

    TestArrivalProcess arrival;
    TestRealDist       dist;
    TestModel          model;

    @BeforeEach
    public void setUp() throws Exception {

        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);
        this.dist = new TestRealDist(model, "Test Distribution", false, false);
        this.arrival = new TestArrivalProcess(model, "First Test ArrivalProcess", dist, false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Test checks, if the given ArrivalRate equals the actual ArrivalRate.
     */
    public void testArrivalRate() {
        assertEquals(dist, arrival.getArrivalRate());
    }

    /**
     * This Test checks the given Name of the ArrivalProcess
     */
    public void testName() {
        assertEquals("First Test ArrivalProcess#1", arrival.getName());
    }

    /**
     * This Test checks, if the ArrivalProcess successfully creates a Successor.
     */
    public void testSuccessor() {
        SimProcess sim1 = arrival.createSuccessor();
        assertNotNull(sim1);
    }

}
