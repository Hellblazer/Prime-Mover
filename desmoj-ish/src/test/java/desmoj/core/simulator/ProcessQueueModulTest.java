package desmoj.core.simulator;

import desmoj.implementation.TestModel;
import desmoj.implementation.TestSimProcess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Since it is not possible to generate an TestClass for SimProcessQueue and Queue, this class tests the PrcessQueue
 * only. In this class not the default sort order is chosen, but the order lifo (last in first out).
 * <p>
 * The methods of QueueBased are not tested here. Look at QueueBasedModulTest for that.
 *
 * @author Clara Bluemm, Sascha Winde
 * @see desmoj.core.simulator.ProcessQueue
 * <P>
 * @see TestFramework.core.simulator.QueueModulTest
 * @see TestFramework.core.simulator.QueueBasedModulTest
 */
public class ProcessQueueModulTest {

    public  ProcessQueue<TestSimProcess> queue;
    private TestSimProcess               enty1;
    private TestSimProcess               enty2;
    private TestSimProcess               enty3;
    private Experiment                   experiment;
    private TestModel                    model;

    @BeforeEach
    public void setUp() throws Exception {
        // experiment has to be created to avoid NullpointExcepions
        this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                         java.util.concurrent.TimeUnit.HOURS, null);
        // a model has to be created to avoid NullpointerExceptions
        this.model = new TestModel();
        // and both have to be connected to avoid NullPointerException
        model.connectToExperiment(experiment);
        enty1 = new TestSimProcess(model, "enty1", false);
        enty2 = new TestSimProcess(model, "enty2", false);
        enty3 = new TestSimProcess(model, "enty3", false);
        queue = new ProcessQueue<>(model, "ProcessQueue", QueueBased.LIFO, 2, false, false);
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Should return the first Element of the Queue.
     */
    @Test
    public void testFirst() {
        queue.insert(enty1);
        queue.insert(enty2);
        assertEquals((queue).first(), enty2);
    }

    /**
     * WTF
     */

    public void testFirstCondition() {
        // TODO
    }

    /**
     * Checks whether the right Entity is given back.
     */
    @Test
    public void testGet() {
        queue.insert(enty1);
        queue.insert(enty2);
        assertEquals((queue).get(0), enty2);
        queue.remove(0);
        assertEquals(queue.get(0), enty1);
    }

    /**
     * Tests whether the insert method works fine.
     */
    @Test
    public void testInsert() {
        assertTrue(queue.insert(enty1));
        assertTrue(queue.insert(enty2));
        assertFalse(queue.insert(enty1));
        // limit == 2
        assertFalse(queue.insert(enty3));
        assertFalse(queue.insert(null));
    }

    /**
     * Tests insert After. The first parameter is the one to be inserted.
     */
    @Test
    public void testInsertAfter() {
        queue.insert(enty1);
        assertFalse(queue.insertAfter(enty1, enty2));
        assertTrue(queue.insertAfter(enty2, enty1));
        assertEquals(queue.first(), enty1);
    }

    /**
     * Tests insertBefore. The first parameter is the one to be inserted before the second parameter.
     */
    @Test
    public void testInsertBefore() {
        queue.insert(enty1);
        assertTrue(queue.insertBefore(enty2, enty1));
        assertEquals(queue.first(), enty2);
        assertEquals(queue.first(), enty2);
    }

    /**
     * Returns the last Entity in the Queue.
     */
    @Test
    public void testLast() {
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        assertEquals(queue.last(), enty1);
        queue.remove(queue.last());
        assertEquals(queue.last(), enty2);
    }

    /**
     * Shall return the entity right before the given one.
     */
    @Test
    public void testPred() {
        queue.insert(enty1);
        queue.insert(enty2);
        assertEquals((queue).pred(enty1), enty2);
        // Bug? throws OutOfBounceException
        //		assertEquals(queue.pred(enty2), null);
    }

    /**
     * WTF
     */

    public void testPredCond() {
        // TODO
    }

    /**
     * Tests the method to remove an entry.
     */
    @Test
    public void testRemove() {
        assertEquals((queue).first(), null);
        queue.insert(enty1);
        queue.insert(enty2);
        queue.remove(enty1);
        assertEquals(queue.first(), enty2);
    }

    /**
     * Resets the statistical counter in QueueBased. Minimum and Maximum length will be set to the current number of
     * entrys.
     */
    @Test
    public void testReset() {
        queue.insert(enty1);
        queue.insert(enty2);
        // maximum length was limited to 2 in the constructor
        assertFalse(queue.insert(enty3));
        assertEquals(queue.maxLength(), 2);
        assertEquals(queue.minLength(), 0);
        queue.remove(enty1);
        queue.reset();
        assertEquals(queue.maxLength(), 1);
        assertEquals(queue.minLength(), 1);
    }

    /**
     * Tests whether the right entity is returned. It should be the successor.
     */
    @Test
    public void testSucc() {
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        //		assertEquals(queue.succ(enty3), enty2);
        assertEquals(queue.succ(enty2), enty1);
        queue.remove(enty2);
        // Bug? throws out of BounceException
        //		assertEquals(queue.succ(enty1), null);
    }

    // TODO QueueIterator???

    /**
     * WTF
     */

    public void testSuccCond() {
        // TODO
    }
}
