package desmoj.core.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import desmoj.implementation.TestEntity;

/**
 * In this class we test the methods of QueueListStandard. It gets extended by
 * QueueListLifo or QueueListFifo.
 *
 * @author Clara Bluemm, Sascha Winde
 *
 * @see desmoj.core.simulator.QueueListStandard
 * @see desmoj.core.simulator.QueueListFifo
 * @see desmoj.core.simulator.QueueListLifo
 *
 */
public abstract class QueueListStandardModulTest extends QueueListModulTest {

    private TestEntity enty1;
    private TestEntity enty2;
    private TestEntity enty3;

    /**
     * Checks whether the method validates the sim-process correctly.
     *
     */

    public void CheckProcess() {
        // not worth to make the method public
    }

    /**
     * Tests whether the first entity of the queue is returned.
     */

    public void first() {
        // The method of java.util.LinkedList is used.
    }

    /**
     * Should return the typ of the list as a String. In our case that would be
     * LIFO.
     */

    public void getAbbreviation() {
        // no need since it is a variable only
    }

    /**
     * Should return the entity at the given position.
     */

    public void getEntity() {
        // The method of java.util.LinkedList is used.
    }

    /**
     * Should return the int value of the entities postition in the list.
     */

    public void getInteger() {
        // The method of java.util.LinkedList is used.
    }

    /**
     * Returns the Implementation of QueueListStandard: QueueListLifo or
     * QueueListFifo
     */
    @Override
    public abstract QueueListStandard getTyp();

    /**
     * Returns boolean, whether, there are elements in the queue or not.
     */

    public void isEmpty() {
        // java.util.AbstractCollection is used
    }

    /**
     * Returns the last Entity of the queue.
     */

    public void last() {
        // default declared and uses java.util.LinkedList
    }

    /**
     * WTF
     */

    public void propertyChange() {
    }

    /**
     * Removes the first entity from the Queue.
     */

    public void removeFirst() {
        // declared as default, uses java.util.LinkedList
    }

    /**
     * Shall remove the last element of the Queue.
     */

    public void removeLast() {
        // declared as default and uses java.util.LinkedList
    }

    /**
     * Shall send a warning to the QueueBased. This method is passed on right to
     * desmoj.core.simulator.ModelComponent.
     */

    public void sendWarning() {
        // declared as default
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.enty1 = new TestEntity(super.model, "enty1", false);
        this.enty2 = new TestEntity(super.model, "enty2", false);
        this.enty3 = new TestEntity(super.model, "enty3", false);
    }

    /**
     * Shall return the size of the QueueList.
     */

    public void size() {
        // uses java.util.LinkedList
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * It should return true or false, depending on whether the entity is already
     * part of the queue.
     */
    @Test
    public void testContains() {
        getTyp().insert(enty1);
        boolean a = getTyp().contains(enty1);
        assertTrue(a);
        boolean b = getTyp().contains(enty2);
        assertFalse(b);
        // check nullreferenz
        enty3 = null;
        boolean c = getTyp().contains(enty3);
        assertFalse(c);
    }

    /**
     * Represents a Queue as a String: All contained Entitys are named in it.
     */
    @Test
    public void testCreatesString() {
        assertEquals(getTyp().toString(), "-");
        getTyp().insert(enty1);
        getTyp().insert(enty2);
        if (getTyp() instanceof QueueListLifo) {
            String s = "0:[enty2#1]<br>1:[enty1#1]<br>";
            assertEquals(getTyp().toString(), s);
        }
        if (getTyp() instanceof QueueListFifo) {
            String s = "0:[enty1#1]<br>1:[enty2#1]<br>";
            assertEquals(getTyp().toString(), s);
        }
    }

    /**
     * This method indicates whether the second parameter (=Entity) was sucessfully
     * inserted after the first parameter (=entity).
     */
    @Test
    public void testInsertAfter() {
        getTyp().insert(enty1);
        boolean a = getTyp().insertAfter(enty2, enty1);
        if (getTyp().get(0) == enty1) {
            assertTrue(a);
        } else {
            assertFalse(a);
        }
        assertFalse(getTyp().insertAfter(enty1, null));
        assertFalse(getTyp().insertAfter(enty1, enty1));
        assertFalse(getTyp().insertAfter(null, enty1));
    }

    /**
     * Same as the method above, but inserts the first paramter before the second
     * parameter (=entity).
     */
    @Test
    public void testInsertBefore() {
        getTyp().insert(enty1);
        boolean a = getTyp().insertBefore(enty2, enty1);
        if (getTyp().get(0) == enty2) {
            assertTrue(a);
        } else {
            assertFalse(a);
        }
        assertFalse(getTyp().insertBefore(enty1, null));
        assertFalse(getTyp().insertBefore(enty1, enty1));
        assertFalse(getTyp().insertBefore(null, enty1));
    }

    /**
     * Predecessor is returned
     */
    @Test
    public void testPred() {
        // not contained
        assertEquals(getTyp().pred(enty1), null);
        getTyp().insert(enty1);
        getTyp().insert(enty2);
        if (getTyp() instanceof QueueListFifo) {
            assertEquals(getTyp().pred(enty2), enty1);
            // no predecessor
            assertEquals(getTyp().pred(enty1), null);
        }
        if (getTyp() instanceof QueueListLifo) {
            assertEquals(getTyp().pred(enty1), enty2);
            // no predecessor
            assertEquals(getTyp().pred(enty2), null);
        }
    }

    /**
     * Removes the first occurence from the given entity. If that is successfully:
     * true is returned.
     */
    @Test
    public void testRemoveEntity() {
        assertFalse(getTyp().remove(enty1));
        getTyp().insert(enty1);
        getTyp().insert(enty2);
        assertTrue(getTyp().remove(enty1));
        assertFalse(getTyp().remove(enty1));
        assertTrue(getTyp().remove(enty2));
        assertFalse(getTyp().remove(null));
    }

    /**
     * Removes at the named (int) position. If that is successfully: true is
     * returned.
     */
    @Test
    public void testRemoveInt() {
        assertFalse(getTyp().remove(0));
        getTyp().insert(enty1);
        getTyp().insert(enty2);
        assertTrue(getTyp().remove(0));
        assertTrue(getTyp().remove(0));
        assertFalse(getTyp().remove(0));
        assertFalse(getTyp().remove(null));
    }

    /**
     * Shall return the following Entity in the Queue.
     */
    @Test
    public void testSucc() {
        // not contained
        assertEquals(getTyp().succ(enty1), null);
        getTyp().insert(enty1);
        getTyp().insert(enty2);
        if (getTyp() instanceof QueueListFifo) {
            assertEquals(getTyp().succ(enty1), enty2);
            // no successor
            assertEquals(getTyp().succ(enty2), null);
        }
        if (getTyp() instanceof QueueListLifo) {
            assertEquals(getTyp().succ(enty2), enty1);
            // no successor
            assertEquals(getTyp().succ(enty1), null);
        }
    }

}
