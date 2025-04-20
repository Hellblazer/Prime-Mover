package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This class tests the TimeSpan Operations.
 *
 * @author Sascha Winde, Clara Bluemm
 *
 */
public class TimeSpanTest {

    TimeSpan timeSpan1;
    TimeSpan timeSpan2;
    TimeSpan timeSpanLong;

    @BeforeEach
    public void setUp() throws Exception {
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);

        this.timeSpan1 = new TimeSpan(10);
        this.timeSpan2 = new TimeSpan(5);
        this.timeSpanLong = new TimeSpan(100.0);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Test checks the current TimeSpan as double value
     */
    @Test
    public void testTimeAsDouble() {
        assertEquals(10.0, timeSpan1.getTimeAsDouble());
    }

    /**
     * This Test checks the current TimeSpan as long value
     */
    @Test
    public void testTimeAsLong() {
        assertEquals(100, timeSpanLong.getTimeRounded());
    }

    /**
     * This test checks two timespans to be equal or longer/shorter than one
     * another.
     */
    @Test
    public void testTimeDuration() {
        assertFalse(TimeSpan.isEqual(timeSpan1, timeSpan2));
        assertTrue(TimeSpan.isLonger(timeSpan1, timeSpan2));
        assertTrue(TimeSpan.isLongerOrEqual(timeSpan1, timeSpan2));
        assertFalse(TimeSpan.isLonger(timeSpan2, timeSpan1));
        assertFalse(TimeSpan.isLongerOrEqual(timeSpan2, timeSpan1));
        assertTrue(TimeSpan.isShorter(timeSpan2, timeSpan1));
        assertFalse(TimeSpan.isShorter(timeSpan1, timeSpan2));
    }

}
