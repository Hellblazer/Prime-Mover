package desmoj.core.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This Class tests the SimClock on it's own.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public class SimClockModulTest {

    private SimClock testClock;

    @BeforeEach
    public void setUp() {
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);

        this.testClock = new SimClock("Test Clock");

    }

    @Test
    public void tearDown() {

    }

    /**
     * This Test checks the Simclocks given name.
     */
    @Test
    public void testName() {
        String name = testClock.getName();
        assertEquals("Test Clock_clock", name);
    }

    /**
     * This Test checks the SimClocks given Time as double.
     */
    @Test
    public void testTime() {
        TimeInstant i = new TimeInstant(0);
        assertTrue(i.getTimeAsDouble() == testClock.getTime().getTimeAsDouble());
    }
}
