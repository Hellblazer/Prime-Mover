package test.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueList;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListStandard;
import test.implementation.TestEntity;

/**
 * In this class we test the class QueueListLifo. This class has just one
 * method. Mind the derived methods from the classes below. All these are part
 * of the package desmoj.core.simulator.
 * 
 * @see QueueBased
 * @see QueueList
 * @see QueueListStandard
 * @see QueueListFifo
 * 
 * @author Clara Bluemm, Sascha Winde
 *
 */
public class QueueListLifoModulTest extends QueueListStandardModulTest {

    public TestEntity    enty1;
    public TestEntity    enty2;
    public TestEntity    enty3;
    public TestEntity    enty4;
    public QueueListLifo queue;

    /**
     * This method returns the queue to test
     * 
     */
    @Override
    public QueueListStandard getTyp() {
        return queue;
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        this.queue = new QueueListLifo();
        super.setUp();
//		queue.setQueueBased(new Queue(super.model, "HelpBase", false, false));
        this.enty1 = new TestEntity(super.model, "enty1", false);
        this.enty2 = new TestEntity(super.model, "enty2", false);
        this.enty3 = new TestEntity(super.model, "enty3", false);
        this.enty4 = new TestEntity(super.model, "enty4", false);
    }

    /**
     * Set-Up for Testing
     *
     * @throws java.lang.Exception
     */
    @Override
    @BeforeAll
    public void setUpBeforeClass() throws Exception {

    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Tests the method, which inserts entities into the Queue
     */
    @Test
    public void testInsert() {
        enty2.setQueueingPriority(2);
        enty3.setQueueingPriority(3);
        queue.insert(enty1);
        queue.insert(enty2);
        queue.insert(enty3);
        queue.insert(enty4);
        assertEquals(queue.get(0), enty3);
        assertEquals(queue.get(1), enty2);
        assertEquals(queue.get(2), enty4);
        assertEquals(queue.get(3), enty1);
        // insert an entity twice
        queue.insert(enty4);
//		} catch (Exception e) {
//			String s = e.toString();
//			assertEquals(s.endsWith("method 'contains(Entity e)'."), true);
//		}
        // insert null-entity
        enty1 = null;
//		try {
        queue.insert(enty1);
//		} catch (Exception e) {
//			String t = "hehe";
//			System.out.println(t);
//			assertEquals(t.endsWith("Be sure to only use valid references."),false);
//		}
    }

}
