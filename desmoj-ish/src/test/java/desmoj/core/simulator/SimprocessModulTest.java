package desmoj.core.simulator;

import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This Class tests the functionality provided by SImprocess.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public abstract class SimprocessModulTest {

    public TestModel      model;
    public TestSimProcess simProcess1;
    public TestSimProcess simProcess2;

    @BeforeEach
    public void setUp() throws Exception {
        this.model = new TestModel();
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);
        model.connectToExperiment(experiment);

        this.simProcess1 = new TestSimProcess(model, "First Test SimProcess", false);
        this.simProcess2 = new TestSimProcess(model, "Second Test SimProcess", false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * It is possible to set a simprocess blocked. This Test checks, if a a simprocess is blocked or not.
     */
    @Test
    public void testBlocked() {
        assertFalse(simProcess1.isBlocked());
        simProcess1.setBlocked(true);
        assertTrue(simProcess1.isBlocked());
        simProcess1.setBlocked(false);
        assertFalse(simProcess1.isBlocked());
    }

    /**
     * This test checks if a simprocess is a component or not.
     */
    @Test
    public void testComponent() {
        assertFalse(simProcess1.isComponent());
    }

    /**
     * Two simprocesses can cooperate with one another, this test checks, if two simprocess actually cooperate.
     */
    @Test
    public void testCooperate() {
        assertTrue(simProcess1.canCooperate());
        assertTrue(simProcess2.canCooperate());

    }

    /**
     * This test checks if a simprocess is interrupted or not.
     */
    @Test
    public void testInterrupted() {
        assertFalse(simProcess1.isInterrupted());
    }

    /**
     * This Test checks if the SimProcess is a simple SimProcess
     */
    @Test
    public void testIsSimprocess() {
        assertTrue(simProcess1.isSimProcess());
    }

    @Test
    public void testMasterIsNull() {
        assertNull(simProcess1.getMaster());
    }

    /**
     * This test checks if a simprocess belongs to an model or not.
     */
    @Test
    public void testModel() {
        assertEquals(model, simProcess1.getModel());
        assertSame(model, simProcess1.getModel());
        assertTrue(model == simProcess1.getModel());
    }

    /**
     * This test checks if the simprocess realtime is correct or not.
     */
    @Test
    public void testRealTime() {
        assertTrue(0 == simProcess1.getRealTimeConstraint());
        simProcess1.setRealTimeConstraint(1);
        assertFalse(0 == simProcess1.getRealTimeConstraint());
        assertTrue(1 == simProcess1.getRealTimeConstraint());
    }

    @Test
    public void testSlaveQueue() {
        assertNull(simProcess1.getSlaveWaitQueue());
    }

    @Test
    public void testSupervisiorIsNull() {
        assertNull(simProcess1.getSupervisor());
    }

    /**
     * This test checks if a simprocess is terminated or not.
     */
    @Test
    public void testTeminated() {
        assertFalse(simProcess1.isTerminated());
    }

}
