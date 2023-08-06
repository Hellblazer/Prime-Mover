package desmoj.core.simulator;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Is the class summing up all the collective implementation of different
 * queueing strategies for a queue list. It provides all the basic methods for
 * inserting objects in a queue, retrieving objects from a queue and getting
 * basic informations about the queue. It is used in many kinds of queue
 * implementations e.g. in the classes <code>QueueListFifo</code> and
 * <code>QueueListLifo</code>.
 *
 * @see QueueBased
 * @see Queue
 * @see ProcessQueue
 * @see QueueListFifo
 * @see QueueListLifo
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Justin Neumann
 * @author based upon ideas from Tim Lechler, Soenke Claassen, Johannes
 *         G&ouml;bel
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
public abstract class QueueListStandard<E extends Entity> extends QueueList<E> implements PropertyChangeListener {

    /**
     * An abbreviation to identify the sort of queueing discipline (like FIFO or
     * LIFO or ...)
     */
    protected java.lang.String abbreviation;

    /**
     * Uses the java.util.LinkedList for implementation
     */
    protected java.util.LinkedList<E> queuelist;

    /**
     * Constructs an empty <code>QueueList</code> with no reference to its client
     * QueueBased. This no-arg constructor is necessary to instantiate an object of
     * this class by calling the <code>java.lang.Class.newInstance()</code> method.
     * The reference to the QueueBased object making use of this queue-functionality
     * must be provided later by calling the setQueueBased() method. The initial
     * length is always zero.
     */
    public QueueListStandard() {

        super();

        // the here used java.LinkedList
        queuelist = new LinkedList<>();

        // the here used java.WeakHashMap
        timemap = new HashMap<>();

    }

    /**
     * Returns true if the given Entity is contained in the list, false otherwise.
     *
     * @return boolean : True if the given Entity is contained in the list, false
     *         otherwise
     * @param e Entity : The Entity assumed to be in the list
     */
    @Override
    public boolean contains(E e) {

        if (e == null) { // check for nullreference
            sendWarning("Can not check if the given Entity is contained in QueueListStandardFifo. "
            + "Command ignored!", "Class: QueueListStandardFifo Method: boolean contains(Entity e).",
                        "The Entity reference given as parameter is a null reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        return queuelist.contains(e);

    }

    /**
     * Returns the first entity stored in the list. If the queue is empty,
     * <code>null</code> is returned.
     *
     * @return Entity : The first entity in the list or <code>null</code> if list is
     *         empty
     */
    @Override
    public E first() {
        if (queuelist.isEmpty()) {
            return null;
        } else {
            return queuelist.getFirst();
        }
    }

    /**
     * Returns the position of the named <code>Entity</code>. The first position is
     * 0, the last one size()-1.
     *
     * @return : The position of the <code>Entity</code> or <code>-1</code> if no
     *         such exists.
     */
    @Override
    public int get(E element) {

        return queuelist.indexOf(element);

    }

    /**
     * Returns the <code>Entity</code> queued at the named position. The first
     * position is 0, the last one size()-1.
     *
     * @return Entity : The <code>Entity</code> at the position of <code>int</code>
     *         or <code>null</code> if no such position exists.
     */
    @Override
    public E get(int index) {

        // check if there are elements queued
        if (isEmpty()) {
            sendWarning("Can not remove Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: boolean remove(Entity e).",
                        "The Queue is empty, no Entities are contained.",
                        "Check if an entity is enqueued by calling method " + "contains(Entity e).");
            return null;
        }

        if ((index > this.size() - 1) | (index < 0)) {
            sendWarning("Can not retrieve index. Command ignored.",
                        "Class: QueueListStandard Method: Entity get(int index).",
                        "The index of the method is out of the list range.", "Check correct position in queue.");
            return null;
        }

        return queuelist.get(index);

    }

    /**
     * Returns an abbreviation as a String to identify the sort of queueing
     * discipline (like FIFO or LIFO or ...). Is used to display the queueing
     * discipline in the report of the QueueBased objects.
     *
     * @return java.lang.String : An abbreviation to identify the sort of queueing
     *         discipline (like FIFO or LIFO or ...)
     */
    @Override
    public String getAbbreviation() {

        return abbreviation;
    }

    /**
     * Adds a new Entity to the QueueList. Entities are inserted according to their
     * priority in descending order. The highest priority Entity will always be
     * first in the queue. Entities with same priority are inserted in specified
     * order.
     *
     * Do not forget to call the <code>statisticalInsert()</code> and to set the
     * queue for each entity as you define this method.
     *
     * @param e Entity : The Entity to add to the QueueList
     */
    @Override
    abstract public void insert(E e);

    /**
     * Returns <code>true</code>, if no elements are inside the
     * <code>QueueList</code>,<code>false</code> otherwise
     *
     * @return boolean : true, if no elements are inside the <code>QueueList</code>,
     *         false otherwise
     */
    @Override
    public boolean isEmpty() {

        return queuelist.isEmpty();

    }

    /**
     * Returns the last Entity stored in the QueueList. If the QueueList is empty,
     * <code>null</code> is returned.
     *
     * @return Entity : The last Entity in the list or <code>null</code> if
     *         QueueList is empty
     */
    @Override
    public E last() {
        if (queuelist.isEmpty()) {
            return null;
        } else {
            return queuelist.getLast();
        }
    }

    /**
     * This method will be called every time the Stock (the number of available
     * units) has changed.
     *
     * @param evt java.beans.PropertyChangeEvent : The event specifying the property
     *            that has changed ans its old and new value.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {

        // check if the property expected has changed
        if (evt.getPropertyName() == "avail") {
            // check if anybody is in the queue
            if (!isEmpty()) {
                // get the first process in the queue
                SimProcess next = (SimProcess) first();

                // check if the process is not a null pointer or is not
                // modelcompatible
                if (!checkProcess(next)) {
                    return;
                } // just return

                // is the process scheduled already?
                if (next.isScheduled()) {
                    next.skipTraceNote(); // do not tell in the trace, that we
                    // ...
                    next.cancel(); // get the process from the event-list
                }

                // remember if the process is blocked
                boolean wasBlocked = next.isBlocked();

                // invalidate the block for a moment
                if (wasBlocked) {
                    next.setBlocked(false);
                }

                next.skipTraceNote(); // do not tell in the trace, that we ...
                next.activateAfter(getQueueBased().current()); // schedule this
                // process
                // right after the current

                // set the block back
                if (wasBlocked) {
                    next.setBlocked(true);
                }
            } // end if isEmpty()
        } // end if propertyName == available

    }

    /**
     * Removes the first occurrence of the given Entity from the QueueList. Checks
     * if the given Entity to be removed does apply to all restrictions on this
     * operation. These are :
     * <ul>
     * <li>The given reference to an entity must not be <code>null</code></li>
     * <li>This QueueList must not be empty, otherwise there's nothing to
     * remove</li> If all these restrictions apply, <code>true</code> is returned
     * and the Entity is removed, otherwise <code>false</code> is the return value
     * because the given Entity could not be removed since one of the restrictions
     * above was not met.
     *
     * @return boolean : Is <code>true</code> if the given Entity is contained in
     *         the QueueList, <code>false</code> otherwise
     * @param e Entity : The Entity to be removed from the QueueList
     */
    @Override
    public boolean remove(E e) {

        if (e == null) { // check for null reference
            sendWarning("Can not remove Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: boolean remove(Entity e).",
                        "The Entity reference given as parameter is a null reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        // check if anything can be removed at all
        if (isEmpty()) {
            sendWarning("Can not remove Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: boolean remove(Entity e).",
                        "The Queue is empty, no Entities are contained.",
                        "Check if an entity is enqueued by calling method " + "contains(Entity e).");
            return false;
        }

        if (!this.contains(e)) { // check for element
            sendWarning("Can not return predecessor Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: Entity remove (Entity e).",
                        "The Entity reference 'e' given as parameter is not contained " + "reference.",
                        "Insert e first.");
            return false;
        }

        // remove the first occurring Entity using the java.util.LinkedList
        queuelist.remove(e);
        e.removeQueueBased(this.clientQ); // remove entity's queue note

        statisticalRemove(e); // remove for statistics

        return true; // job done

    }

    /**
     * Removes the <code>Entity</code> queued at the named position. The first
     * position is 0, the last one size()-1.
     *
     * @return : The method returns <code>true</code> as the <code>Entity</code> was
     *         deleted or <code>false></code> if otherwise.
     */
    @Override
    public boolean remove(int index) {
        if (!this.contains(this.get(index))) { // check for element
            sendWarning("Can not return predecessor Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: Entity remove (int index).",
                        "The Entity reference 'e' given as parameter is not contained " + "reference.",
                        "Insert element first.");
            return false;
        }

        E e = queuelist.remove(index);

        if (e == null) // if nothing has been removed
        {
            return false;
        }
        e.removeQueueBased(this.clientQ); // remove entity's queue note

        statisticalRemove(e); // remove for statistics

        return true;

    }

    /**
     * Returns the successor to the given Entity in the QueueList. If there is no
     * successor or no Entity in the QueueList, <code>null</code> is returned.
     *
     * @return Entity : The Entity before the given parameter in the QueueList or
     *         <code>null</code> if the given Entity parameter 'e' has no successor
     *         in the QueueList or e itself is not contained in the QueueList
     * @param e Entity : The Entity contained in the QueueList
     */
    @Override
    public E succ(E e) {

        if (e == null) // check for null reference
        {
            sendWarning("Can not return successing Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: Entity succ (Entity e).",
                        "The Entity reference 'e' given as parameter is a null " + "reference.",
                        "Check if Entity 'e' is enqueued using method " + "'QueueListStandardFifo.contains(e)'.");
            return null;
        }

        if (!this.contains(e) || e.equals(queuelist.getLast())) // check for last element
        {
            return null;
        }

        return queuelist.get(queuelist.indexOf(e) + 1);

    }

    /**
     * Returns a string representation of the QueueList. The string is built by
     * concatenating all string representations of the contained entities, calling
     * their <code>toString()</code> methods.
     *
     * @return java.lang.String : The string representation of the QueueList
     */
    @Override
    public String toString() {
        String s = "";

        for (int i = 0; i < this.size(); i++) {
            Entity e = this.queuelist.get(i);
            s = s + i + ":[" + e + "]<br>";
        }

        if (isEmpty()) {
            s = "-";
        }

        return s;

    }

    /**
     * Checks whether the process using the QueueList is a valid SimProcess.
     *
     * @return boolean : Returns whether the SimProcess is valid or not.
     * @param proc SimProcess : Is this SimProcess a valid one?
     */
    protected boolean checkProcess(SimProcess proc) {

        if (proc == null) // if proc is a null pointer instead of a process
        {
            sendWarning("A non existing process was stored in a queue. " + "The attempted action is ignored!",
                        "QueueListStandardFifo, Method: first(); called by Class: Stock, Method: "
                        + "store(long n) or retrieve(long n)", "The process is only a null pointer.",
                        "Make sure that only real SimProcesses are stored in a queue.");
            return false;
        }

        if (!getQueueBased().isModelCompatible(proc)) // if proc is not
        // modelcompatible
        {
            sendWarning("The process trying to use a QueueList object does not "
            + "belong to this model. The attempted action is ignored!",
                        "QueueListStandardFifo, Method: first(); called by Class: Stock, Method: "
                        + "store(long n) or retrieve(long n)", "The process is not modelcompatible.",
                        "Make sure that processes are using only queues within their model.");
            return false;
        }

        return true;
    }

    /**
     * Inserts the given "e" after the position of "which" in the QueueList. Returns
     * true if "e" is inserted correctly after "which". If the list is empty or the
     * referenced "which" is not contained, the "e" will not be inserted and false
     * is returned.
     *
     * @return boolean : Is <code>true</code> if inserted correctly,
     *         <code>false</code> otherwise
     * @param e     Entity : The Entity to be inserted
     * @param which Entity : The referenced Entity that the given "e" has to be
     *              inserted after
     */
    @Override
    boolean insertAfter(E e, E which) {

        if (e == null) { // check for null reference
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: boolean insertAfter(Entity e, " + "Entity which).",
                        "The Entity reference 'e' given as parameter is a null " + "reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        if (which == null) { // check for null reference
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: boolean insertAfter(Entity e, " + "Entity which).",
                        "The Entity reference 'which' given as parameter is a null " + "reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        if (contains(e)) { // entity must not be contained twice in the queue
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: boolean insertAfter(Entity e, " + "Entity which).",
                        "The Entity 'e' given as parameter is already enqueued.",
                        "Make sure the entity is not enqueued here by calling " + "method 'contains(Entity e)'.");
            return false;
        }

        queuelist.add(queuelist.indexOf(which) + 1, e); // is adding the entity
        e.addQueueBased(this.clientQ); // sets entity's queue as this queued

        statisticalInsert(e); // for statistics

        return true; // inserted

    }

    /**
     * Inserts the given "e" before the position of "which" in the QueueList.
     * Returns true if "e" is inserted correctly after "which". If the list is empty
     * or the referenced "which" is not contained, the "e" will not be inserted and
     * false is returned.
     *
     * @return boolean : Is <code>true</code> if inserted correctly,
     *         <code>false</code> otherwise
     * @param e     Entity : The Entity to be inserted
     * @param which Entity : The referenced Entity that the given "e" has to be
     *              inserted before
     */
    @Override
    boolean insertBefore(E e, E which) {

        if (e == null) { // check for null reference
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: insertBefore(Entity e, " + "Entity which).",
                        "The Entity reference 'e' given as parameter is a null " + "reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        if (which == null) { // check for null reference
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: insertBefore(Entity e, " + "Entity which).",
                        "The Entity reference 'which' given as parameter is a null " + "reference.",
                        "Be sure to only use valid references.");
            return false;
        }

        if (contains(e)) { // entity must not be contained twice in queue
            sendWarning("Can not insert entity. Command ignored.",
                        "Class 'QueueListStandardFifo' Method: insertBefore(Entity e, " + "Entity which).",
                        "The Entity 'e' given as parameter is already enqueued.",
                        "Make sure the entity is not enqueued here by calling " + "method 'contains(Entity e)'.");
            return false;
        }

        queuelist.add(queuelist.indexOf(which), e); // add on top as the rest is being shifted
        e.addQueueBased(this.clientQ); // sets entity's queue as this queued

        statisticalInsert(e); // for statistics

        return true; // inserted

    }

    /**
     * Returns the predecessor to the given Entity in the QueueList. If there is no
     * predecessor or no Entity, <code>null</code> is returned.
     *
     * @return Entity : The Entity before the given parameter in the QueueList or
     *         <code>null</code> if e has no predecessor in the QueueList or Entity
     *         parameter 'e' itself is not contained
     * @param e Entity : The Entity contained in the QueueList whose predecessor
     *          will be returned.
     */
    @Override
    E pred(E e) {

        if (e == null) // check for null reference
        {
            sendWarning("Can not return predecessor Entity. Command ignored.",
                        "Class: QueueListStandardFifo Method: Entity pred (Entity e).",
                        "The Entity reference 'e' given as parameter is a null " + "reference.",
                        "Check if Entity 'e' is enqueued using method " + "'QueueListStandardFifo.contains(e)'.");
            return null;
        }

        if (!this.contains(e) || e.equals(queuelist.getFirst())) // check for first element
        {
            return null;
        }

        return queuelist.get(queuelist.indexOf(e) - 1);

    }

    /**
     * Removes the first entity from this QueueList and returns <code>true</code> if
     * it was removed successfully. If the QueueList is empty, <code>false</code> is
     * returned.
     *
     * @return boolean : Is <code>true</code>, if the first element has been removed
     *         successfully, <code>false</code> if the QueueList happened to be
     *         empty.
     */
    @Override
    boolean removeFirst() {

        if (isEmpty()) {
            sendWarning("Can not remove first entity in queue. Command ignored.",
                        "Class: QueueListStandardFifo Method: boolean removeFirst().",
                        "The queue is empty, thus no Entity can be removed.",
                        "Check if any Entity 'e' is enqueued using method " + "'QueueListStandardFifo.contains(e)'.");
            return false; // nothing from nothing leaves nothing
        }

        return remove(first()); // delegate to remove()

    }

    /**
     * Removes the last Entity from the QueueList and returns <code>true</code> if
     * it was removed successfully. If the QueueList is empty, <code>false</code> is
     * returned.
     *
     * @return boolean : Is <code>true</code>, if the last element has been removed
     *         successfully, <code>false</code> if the QueueList happened to be
     *         empty
     */
    @Override
    boolean removeLast() {

        if (isEmpty()) {
            sendWarning("Can not remove last Entity in queue. Command ignored.",
                        "Class: QueueListStandardFifo Method: boolean removeLast().",
                        "The queue is empty, thus no Entity can be removed.",
                        "Check if any Entity 'e' is enqueued using method " + "'QueueListStandardFifo.contains(e)'.");
            return false; // nothing from nothing leaves nothing
        }

        return remove(last()); // delegate to remove()

    }

    /**
     * Sends a warning to the error output by forwarding it to the associated
     * QueueBased's <code>sendwarning</code> method. Warnings are sent only if the
     * QueueBased's flag for queue implementation warnings is set to
     * <code>true</code>.
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
    @Override
    void sendWarning(String description, String location, String reason, String prevention) {

        if (clientQ.qImpWarn()) {
            clientQ.sendWarning(description, location, reason, prevention);
        }

    }

}
