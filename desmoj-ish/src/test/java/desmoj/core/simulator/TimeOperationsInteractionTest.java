package desmoj.core.simulator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This Test checks the Timeoperations of Timeinstants and Timespan
 *
 * @author Sascha Winde, Clara Bluemm
 */
public class TimeOperationsInteractionTest {

    TimeInstant timeInstant1;
    TimeInstant timeInstant2;
    TimeSpan    timeSpan1;
    TimeSpan    timeSpan2;

    @BeforeEach
    public void setUp() throws Exception {
        Experiment experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                               java.util.concurrent.TimeUnit.HOURS, null);

        this.timeInstant1 = new TimeInstant(10);
        this.timeInstant2 = new TimeInstant(5);
        this.timeSpan1 = new TimeSpan(10);
        this.timeSpan2 = new TimeSpan(5);

    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Time classes can be added. This test checks the result if you add a TimeInstant/TimeSpan to another
     * TimeInstant/TimeSpan.
     */
    @Test
    public void testTimeAdd() {
        TimeInstant a = TimeOperations.add(timeInstant1, timeSpan1);
        assertTrue(20 == a.getTimeAsDouble());
        a = TimeOperations.add(timeSpan2, timeInstant2);
        assertTrue(10 == a.getTimeAsDouble());
        TimeSpan b = TimeOperations.add(timeSpan1, timeSpan2);
        assertTrue(15 == b.getTimeAsDouble());
    }

    /**
     * Time classes can be differed. This test checks the result if you differ a TimeInstant/TimeSpan with another
     * TimeInstant/TimeSpan.
     */
    @Test
    public void testTimeDiff() {
        TimeSpan a = TimeOperations.diff(timeInstant1, timeInstant2);
        assertTrue(5 == a.getTimeAsDouble());
        a = TimeOperations.diff(timeSpan1, timeSpan2);
        assertTrue(5 == a.getTimeAsDouble());
    }

    /**
     * Time classes can be divided. This test checks the result if you divide a TimeInstant/TimeSpan with another
     * TimeInstant/TimeSpan.
     */
    @Test
    public void testTimeDivide() {
        assertTrue(5 == TimeOperations.divide(timeSpan1, 2).getTimeAsDouble());
        TimeSpan a = TimeOperations.divide(timeSpan2, 5);
        assertTrue(1 == a.getTimeAsDouble());
    }

    /**
     * Time classes can be multiply. This test checks the result if you multiply a TimeInstant/TimeSpan with another
     * TimeInstant/TimeSpan.
     */
    @Test
    public void testTimeMultiply() {
        TimeSpan a = TimeOperations.multiply(2, timeSpan1);
        assertTrue(20 == a.getTimeAsDouble());
        a = TimeOperations.multiply(timeSpan2, 2);
        assertTrue(10 == a.getTimeAsDouble());
    }

}
