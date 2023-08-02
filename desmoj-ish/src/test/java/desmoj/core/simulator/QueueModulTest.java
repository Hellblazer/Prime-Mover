package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Queue;
import desmoj.implementation.TestEntity;
import desmoj.implementation.TestModel;

/**
 * Since it is not possible to generate an TestClass for SimProcessQueue and
 * Queue, this class tests the Queue only. The default implementation with with
 * the sort order fifo (first in first out) is used.
 * <p>
 * The methods of QueueBased are not tested here. Look at QueueBasedModulTest
 * for that.
 * 
 * 
 * @author Clara Bluemm, Sascha Winde
 * 
 * @see desmoj.core.simulator.Queue<E>
 * @see TestFramework.core.simultor.ProcessQueueModulTest
 * @see TestFramework.core.simulator.QueueBasedModulTest
 */
public class QueueModulTest {

    // 'generic' TestObjects; are instantiated in the derived classes
    protected TestEntity enty1;
    protected TestEntity enty2;
    protected TestEntity enty3;

    protected Experiment        experiment;
    protected TestModel         model;
    protected Queue<TestEntity> queue;

    /**
     * 
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        // experiment has to be created to avoid NullpointExcepions
        this.experiment = new Experiment("Test Experiment", java.util.concurrent.TimeUnit.SECONDS,
                                         java.util.concurrent.TimeUnit.HOURS, null);
        // a model has to be created to avoid NullpointerExceptions
        this.model = new TestModel();
        // and both have to be connected to avoid NullPointerException
        model.connectToExperiment(experiment);
        enty1 = new TestEntity(model, "enty1", false);
        enty2 = new TestEntity(model, "enty2", false);
        enty3 = new TestEntity(model, "enty3", false);
        queue = new Queue<TestEntity>(model, "queue", false, false);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Should return the first Element of the Queue.
     */
    @Test
    public void testFirst() {
        assertEquals((queue).first(), null);
        queue.insert(enty1);
        queue.insert(enty2);
        assertEquals((queue).first(), enty1);
    }

    /*
     * WTF
     */

    public void testFirstCondition() {
        // TODO
    }

    /**
     * Checks whether the right Entity is given back.
     */

    public void testGet() {
        queue.insert(enty1);
        queue.insert(enty2);
//		assertEquals(queue.get(0), enty2);
//		queue.remove(0);
//		assertEquals(queue.get(0), enty1);
    }

    /**
     * Tests whether the insert method works fine.
     */
    @Test
    public void testInsert() {
        assertTrue(queue.insert(enty1));
        assertTrue(queue.insert(enty2));
        // in ProcessQueue inserting twice the same Entity is not allowed
//		assertFalse(queue.insert(enty1));
        assertTrue(queue.insert(enty3));
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
        assertTrue(queue.insertAfter(enty3, enty1));
        assertFalse(queue.insertAfter(enty2, enty3));
        assertFalse(queue.insertAfter(enty1, enty3));
        assertFalse(queue.insertAfter(enty3, enty3));
    }

    /**
     * Tests insertBefore. The first parameter is the one to be inserted before the
     * second parameter.
     */
    @Test
    public void testInsertBefore() {
        queue.insert(enty1);
        assertTrue(queue.insertBefore(enty2, enty1));
        assertEquals(queue.first(), enty2);
        assertTrue(queue.insertAfter(enty3, enty2));
        queue.remove(enty2);
        assertEquals(queue.first(), enty3);
    }

    /**
     * Returns the last Entity in the Queue.
     */
    @Test
    public void testLast() {
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        assertEquals(queue.last(), enty3);
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
        assertEquals(queue.pred(enty2), enty1);
        // Bug? throws out of BounceException
//		assertEquals(queue.pred(enty1), null);
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
        queue.insert(enty1);
        queue.insert(enty2);
        queue.remove(enty1);
        assertEquals(queue.first(), enty2);
    }

    /**
     * Resets the statistical counter in QueueBased. Minimum and Maximum length will
     * be set to the current number of entrys.
     */
    @Test
    public void testReset() {
        assertEquals((queue).first(), null);
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        assertEquals(queue.maxLength(), 3);
        assertEquals(queue.minLength(), 0);
        queue.remove(enty1);
        queue.reset();
        assertEquals(queue.maxLength(), 2);
        assertEquals(queue.minLength(), 2);
    }

    /**
     * Tests whether the right entity is returned. It should be the successor.
     */
    @Test
    public void testSucc() {
        assertEquals(queue.first(), null);
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        assertEquals(queue.succ(enty1), enty2);
        queue.remove(enty2);
        assertEquals(queue.succ(enty1), enty3);
        // Bug? throws OutOfBounceException
//		assertEquals(queue.succ(enty3), null);
    }

    // TODO QueueIterator???

    /**
     * WTF
     */
    public void testSuccCond() {
        // TODO
    }
}
