package desmoj.core.simulator;

import java.beans.PropertyChangeListener;
import java.util.Iterator;

import desmoj.core.exception.SimAbortedException;
import desmoj.core.report.ErrorMessage;

/**
 * Is the abstract superclass for all the classes implementing different
 * queueing strategies for a waiting-queue. It provides all the basic methods
 * for inserting objects in a queue, retrieving objects from a queue and getting
 * basic informations about the queue. It is used in many kinds of queue
 * implementations where collective functionalities are implemented by
 * <code>QueueListStandard</code> and are specified e.g. in
 * <code>QueueListFifo</code> or <code>QueueListLifo</code>.
 *
 * @see QueueBased
 * @see Queue
 * @see ProcessQueue
 * @see QueueListFifo
 * @see QueueListLifo
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author based upon ideas from Tim Lechler
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
public abstract class QueueList<E extends Entity> implements PropertyChangeListener, Iterable<E> {

    /**
     * Private queue list iterator, e.g. required for processing all queue elements
     * in a for-each-loop.
     */
    private class QueueListIterator implements Iterator<E> {

        QueueList<E> clientQ;
        E            next, lastReturned;

        public QueueListIterator(QueueList<E> clientQ) {
            this.clientQ = clientQ;
            next = clientQ.first();
            lastReturned = null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            lastReturned = next;
            next = clientQ.succ(next);
            return lastReturned;
        }

        @Override
        public void remove() {
            clientQ.remove(lastReturned);
        }
    }

    /**
     * The QueueBased object this queuelist serves as a container for.
     */
    protected QueueBased clientQ;

    /**
     * Uses the java.util.WeakHashMap functionalities to link entities with their
     * entry time.
     */
    protected java.util.HashMap<E, TimeInstant> timemap;

    /**
     * Should return <code>true</code> if the given <code>Entity</code> is contained
     * in the queue; <code>false</code> otherwise.
     *
     * @return boolean : Should be <code>true</code> if the given
     *         <code>Entity</code> is contained in the queue; <code>false</code>
     *         otherwise.
     * @param e E : The <code>Entity</code> we are looking for in the queue.
     */
    public abstract boolean contains(E e);

    /**
     * Should return the first entity in the queue.
     *
     * @return E : The first entity in the queue
     */
    public abstract E first();

    /**
     * Returns the position of the named <code>Entity</code>. The first position is
     * 0, the last one size()-1.
     *
     * @return : The position of the <code>Entity</code> or <code>-1</code> if no
     *         such exists.
     */
    public abstract int get(E element);

    /**
     * Returns the <code>Entity</code> queued at the named position. The first
     * position is 0, the last one size()-1.
     *
     * @return E : The <code>Entity</code> at the position of <code>int</code> or
     *         <code>null</code> if no such position exists.
     */
    public abstract E get(int index);

    /**
     * Should return an abbreviation as a String to identify the sort of queueing
     * discipline (like FIFO or LIFO or ...). Is used to display the queueing
     * discipline in the report of the QueueBased objects.
     *
     * @return java.lang.String : An abbreviation to identify the sort of queueing
     *         discipline (like FIFO or LIFO or ...)
     */
    public abstract String getAbbreviation();

    /**
     * Should add a new Entity to the queue.
     *
     * @param e E : The Entity which will be added to the queue
     */
    public abstract void insert(E e);

    /**
     * Should return <code>true</code> if no entities are stored in the queue at the
     * moment, <code>false</code> otherwise.
     *
     * @return boolean : Is <code>true</code> if no entities are stored in the queue
     *         at the moment, <code>false</code> otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns an iterator over the entities enqueued.
     *
     * @return java.lang.Iterator&lt;E&gt; : An iterator over the entities enqueued.
     */
    @Override
    public Iterator<E> iterator() {
        return new QueueListIterator(this);
    }

    /**
     * Should return the last <code>Entity</code> in the queue.
     *
     * @return E : The last <code>Entity</code> in the queue.
     */
    public abstract E last();

    /**
     * Should remove the given <code>Entity</code> "e" from the queue. If this is
     * done successfully <code>true</code> is returned, <code>false</code>
     * otherwise.
     *
     * @return boolean : Is <code>true</code> if the given <code>Entity</code> is
     *         removed successfully, <code>false</code> otherwise.
     * @param e E : The <code>Entity</code> which is to be removed from the queue.
     */
    public abstract boolean remove(E e);

    /**
     * Removes the <code>Entity</code> queued at the named position. * The first
     * position is 0, the last one size()-1.
     *
     * @return : The method returns <code>true</code> as the <code>Entity</code>
     *         exists or <code>false></code> if otherwise.
     */
    public abstract boolean remove(int index);

    /**
     * Sets the client queue for which the entities are stored. Is needed, because
     * this can not be done in the no-arg constructor.
     *
     * @param queueBase desmoj.QueueBased : The QueueBased using this queueing
     *                  system.
     */
    public void setQueueBased(QueueBased queueBase) {

        // DOA if no real QueueBased object is given
        if (queueBase == null) {
            throw (new SimAbortedException(new ErrorMessage(null,
                                                            "Can not create QueueListStandardFifo! Simulation aborted.",
                                                            "Class : QueueListStandardFifo / Method : setClientQueue"
                                                            + "(QueueBased queueBase) ",
                                                            "The reference to the QueueBased object needed is a null "
                                                            + "reference.",
                                                            "Always check to give valid references only.", null)));
        }

        clientQ = queueBase; // set reference to my client using me as a
        // container

    }

    /**
     * Returns the actual size of the QueueList.
     *
     * @return : The method returns the size as an <code>int</code>. The value is 0
     *         if no Entity is in line.
     */
    public int size() {
        return clientQ.length();
    }

    /**
     * Should return the successor of the given <code>Entity</code> "e" in the
     * queue.
     *
     * @return E : The <code>Entity</code> after the given <code>Entity</code> "e"
     *         in the queue.
     * @param e E : The successor of this <code>Entity</code> will be returned.
     */
    public abstract E succ(E e);

    /**
     * Should return a string representation of the queue.
     *
     * @return String : The string representation of the queue.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the <code>QueueBased</code> object this <code>QueueList</code> serves
     * as a queue implementation for.
     *
     * @return QueueBased : The <code>QueueBased</code> object this
     *         <code>QueueList</code> serves as a container for.
     */
    QueueBased getQueueBased() {
        return clientQ;
    }

    /**
     * Should insert the <code>Entity</code> "e" right after the position of
     * <code>Entity</code> "which" in the queue. Should return <code>true</code> if
     * this was done successfully. Be careful with this operation. It might disrupt
     * your order of priorities.
     *
     * @return boolean : Is <code>true</code> if insertion was successfull,
     *         <code>false</code> otherwise.
     * @param e     E : The <code>Entity</code> which will be inserted.
     * @param which E : The <code>Entity</code> determining the position after which
     *              the <code>Entity</code> "e" will be inserted in the queue.
     */
    abstract boolean insertAfter(E e, E which);

    /**
     * Should insert the <code>Entity</code> "e" right before the position of
     * <code>Entity</code> "which" in the queue. Should return <code>true</code> if
     * this was done successfully. Be careful with this operation. It might disrupt
     * your order of priorities.
     *
     * @return boolean : Is <code>true</code> if insertion was successfull,
     *         <code>false</code> otherwise.
     * @param e     E : The <code>Entity</code> which will be inserted.
     * @param which E : The <code>Entity</code> determining the position before
     *              which the <code>Entity</code> "e" will be inserted in the queue.
     */
    abstract boolean insertBefore(E e, E which);

    /**
     * Should return the predecessor of the given <code>Entity</code> "e" in the
     * queue.
     *
     * @return E : The <code>Entity</code> before the given <code>Entity</code> "e"
     *         in the queue.
     * @param e E : The predecessor of this <code>Entity</code> will be returned.
     */
    abstract E pred(E e);

    /**
     * Should remove the first <code>Entity</code> in the queue and returns
     * <code>true</code> if done successfully, <code>false</code> otherwise.
     *
     * @return boolean : Is <code>true</code> if the first <code>Entity</code> in
     *         the queue is removed successfully, <code>false</code> otherwise.
     */
    abstract boolean removeFirst();

    /**
     * Should remove the last <code>Entity</code> in the queue and returns
     * <code>true</code> if done successfully, <code>false</code> otherwise.
     *
     * @return boolean : Is <code>true</code> if the last <code>Entity</code> in the
     *         queue is removed successfully, <code>false</code> otherwise.
     */
    abstract boolean removeLast();

    /**
     * Should send a warning to the error output by forwarding it to the associated
     * <code>QueueBased's sendWarning</code> method. Warnings should only be sent if
     * the <code>QueueBased</code>'s flag for queue implementation warnings is set
     * to <code>true</code>.
     *
     * @param description java.lang.String : describing the error
     * @param location    java.lang.String : describing the location the error
     *                    occurred
     * @param reason      java.lang.String : describing the possible cause for this
     *                    error
     * @param prevention  java.lang.String : telling what to do to prevent this
     *                    error
     * @see QueueBased
     */
    abstract void sendWarning(String description, String location, String reason, String prevention);

    /**
     * This method is used for statistical data in the class QueueBased.
     *
     */
    void statisticalInsert(E e) {

        timemap.put(e, clientQ.presentTime()); // saves time of insertion

        clientQ.addItem(); // update statistics
    }

    /**
     * This method is used for statistical data in the class QueueBased.
     *
     */
    void statisticalRemove(E e) {
        clientQ.deleteItem(timemap.get(e)); // tell QueueBased the entry time for update
        // statistics

        timemap.remove(e); // removes the entity from timemap
    }
}
