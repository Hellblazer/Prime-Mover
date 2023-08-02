package desmoj.core.simulator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import desmoj.core.simulator.QueueBased;
import desmoj.core.simulator.QueueListFifo;
import desmoj.core.simulator.QueueListLifo;
import desmoj.core.simulator.QueueListStandard;

/**
 * In this class we test the class QueueList. This class has just two methods
 * but declares many as abstract. These get implementd in QueueListStandard.
 * Mind the derived methods from the classes named below. All these are part of
 * the package desmoj.core.simulator.
 * 
 * @see QueueBased
 * @see QueueListStandard
 * @see QueueListLifo
 * @see QueueListFifo
 * 
 * @author Clara Bluemm, Sascha Winde
 *
 */
public abstract class QueueListModulTest extends QueueBasedModulTest {

    /**
     * Returns the <code>QueueBased</code> object the <code>QueueList</code> serves
     * as a queue implementation for.
     */

    public void getQueueBased() {
        // not necessary, since it returns an object only
    }

    /**
     * Sets the client queue for which the entities are stored. Is needed, because
     * this can not be done in the no-arg constructor.
     */

    public void setQueueBased() {
        // not necessary, since it sets an object only
    }

    /**
     * Set-Up for Testing
     *
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {

    }
}
