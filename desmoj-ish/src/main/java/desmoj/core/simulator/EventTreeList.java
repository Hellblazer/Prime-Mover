package desmoj.core.simulator;

import java.util.Iterator;

import org.apache.commons.collections4.list.TreeList;

import desmoj.core.exception.SimAbortedException;
import desmoj.core.report.ErrorMessage;

/**
 * Alternative Implementation of the interface <code>EventList</code> using a
 * tree-based list as a container for the event-notes, yielding both access and
 * removal of event-list entries in O(log n) time.
 *
 * Disadvantages compared to <code>EventVector</code> include
 * non-thread-safeness (however, discrete Event simulation should never attempt
 * concurrent modifications of the event-list) and the slightly higher memory
 * requirement.
 *
 * The internal tree-based list is provided by the class
 * <code>org.apache.commons.collections.list.TreeList</code> from the Commons
 * Collections package from the Apache Jakarta Commons Project (see
 * http://jakarta.apache.org/commons/index.html). Thus, his product includes
 * software developed by The Apache Software Foundation
 * (http://www.apache.org/). For License see
 * http://www.apache.org/licenses/LICENSE-2.0 (of which a copy can be found in
 * the root directory of this distribtuon).
 *
 * @see org.apache.commons.collections.list.TreeList
 * @see EventVectorList
 * @see EventNote
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Ruth Meyer, modified by Johannes Göbel
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
public class EventTreeList extends EventList {

    /**
     * The tree list container used to store the event-notes.
     */
    TreeList eTreeList; // The tree list containing all Event notes.

    /**
     * Constructs an empty event-list.
     */
    EventTreeList() {

        // create event-list
        eTreeList = new TreeList();

    }

    /**
     * Returns if the event-list processes concurrent Events in random order or not.
     *
     * @return boolean: <code>false</code> since no randomization
     */
    @Override
    public boolean isRandomizingConcurrentEvents() {
        return false;
    }

    /**
     * Returns a string representing the entries of this tree list in a row. The
     * resulting string includes all Event notes in ascending order as they are
     * placed inside the event tree list.
     */
    @Override
    public String toString() {

        StringBuffer textBuffer = new StringBuffer();
        // faster than String and '+'
        Iterator<?> notes = eTreeList.iterator(); // get all elements

        while (notes.hasNext()) { // loop through all elements
            textBuffer.append("[");
            textBuffer.append(notes.next());
            textBuffer.append("]"); // compose Note
        }

        return textBuffer.toString();
        // return String representation of StringBuffer

    }

    /**
     * Returns the first event-note in the event-list. It is the event-note with the
     * lowest (nearest) associated point of simulation time of all Event notes
     * contained in the evnet-list. Note that the event-note is not removed from the
     * event-list.
     *
     * @return EventNote : the event-note to be processed next in the order of time.
     *         Returns <code>null</code> if the event-list is empty.
     */
    @Override
    EventNote firstNote() {

        if (isEmpty())
            return null; // nothing there, nothing returned
        else
            return (EventNote) eTreeList.get(0); // return but not remove

    }

    /**
     * Inserts the new event-note preserving the temporal order of the event-notes
     * contained in the event-list. It uses binary search to determine the position
     * where to insert the new event-note to increase performance.
     *
     * @param newNote EventNote : the new note to be inserted in the event-list
     *                keeping the temporal order
     */
    @Override
    void insert(EventNote newNote) {

        // code for adding EventNote to all possible entities

        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }

        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }

        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }

        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }

        if (isEmpty()) { // no worry about order if it's first
            eTreeList.add(newNote); // easy if empty ;-)
            return; // no need to continue
        } else { // now here comes the binary sorting

            int left = 0; // left border of search partition
            int right = eTreeList.size() - 1; // right border of search
                                              // partition
            int index = 0; // current position in tree list
            TimeInstant refTime = newNote.getTime();
            long refPrio = newNote.getPriority();
            // shortcut for call to newNote

            do {
                index = (left + right) / 2; // center on searchable partition
                // check if EventNote at index has smaller or equal time

                if (TimeInstant.isBefore(((EventNote) eTreeList.get(index)).getTime(), refTime) ||
                    (TimeInstant.isEqual(((EventNote) eTreeList.get(index)).getTime(), refTime) &&
                     ((EventNote) eTreeList.get(index)).getPriority() >= refPrio)) {
                    if (index < (eTreeList.size() - 1)) {
                        // is there a note to the right
                        if (TimeInstant.isAfter(((EventNote) eTreeList.get(index + 1)).getTime(), refTime) ||
                            (TimeInstant.isEqual(((EventNote) eTreeList.get(index + 1)).getTime(), refTime) &&
                             ((EventNote) eTreeList.get(index)).getPriority() < refPrio)) {
                            // if note to the right is larger
                            eTreeList.add(index + 1, newNote);
                            // found position
                            return; // everything done, no need to continue
                        } else {
                            left = index + 1;
                            // no hit, so set new boundaries and go on
                        }
                    } // no Event notes right of the index, so all
                    else { // notes are smaller, thus append to end
                        eTreeList.add(newNote);
                        return; // everything done, get out of here fast!
                    }
                } else { // EventNote at index has larger time
                    if (index > 0) { // is there a note left of the index?
                        if (TimeInstant.isBefore(((EventNote) eTreeList.get(index - 1)).getTime(), refTime) ||
                            (TimeInstant.isEqual(((EventNote) eTreeList.get(index - 1)).getTime(), refTime) &&
                             ((EventNote) eTreeList.get(index - 1)).getPriority() >= refPrio)) {
                            // if note to the left is smallerOrEqual
                            eTreeList.add(index, newNote);
                            // found position
                            return; // everything done, no need to continue
                        } else {
                            right = index - 1;
                            // no hit, so set new boundaries and go on
                        }
                    } // no Event notes left of the index, so all
                    else { // notes are larger, thus insert at pos. 0
                        eTreeList.add(0, newNote);
                        return; // everything done, get out of here!
                    }
                }
            } while ((left <= right));
            eTreeList.add(newNote);
        }
    }

    /**
     * Inserts a new event-note after another EventNote specified. Note that to keep
     * the temporal order of the event-list, the scheduled time will be set to the
     * same time as the referred "afterNote". Note also, that afterNote must be
     * contained in the event-list. If the referred "where" is not contained in the
     * event-list, there is no chance to determine the time that the new note is
     * intended to be scheduled at. Thus the new event-note will not be inserted and
     * a <code>EventNotScheduledException</code> will be thrown, stopping the
     * simulation.
     *
     * @param where   EventNote : The event-note containing the event after which
     *                the new note is supposed to be inserted into the event-list.
     * @param newNote EventNote : The new event-note to be inserted after the
     *                specified EventNote in the event-list.
     * @throws SimAbortedException : if referred EventNote is not contained in the
     *                             event-list
     */
    @Override
    void insertAfter(EventNote where, EventNote newNote) {

        // code for adding EventNote to all possible entities

        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }

        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }

        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }

        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }

        int i = eTreeList.indexOf(where);

        if (i < 0) { // negative index means, that where is not contained
            Model mBuffer = null; // buffer current model
            if (newNote.getEntity1() != null) {
                mBuffer = newNote.getEntity1().getModel();
            }
            if (newNote.getEvent() != null) {
                mBuffer = newNote.getEvent().getModel();
            }
            throw new SimAbortedException(new ErrorMessage(mBuffer,
                                                           "Can not insert new event-note after given EventNote! "
                                                           + "Simulation aborted",
                                                           "Internal DESMO-J class : EventTreeList Method : "
                                                           + "insertAfter(EventNote where, EventNote newNote)",
                                                           "The event-note to insert the new note after is not contained "
                                                           + "in the event tree list.",
                                                           "This is a fatal error. Contact DESMOJ support",
                                                           newNote.getTime()));
        } else { // if where is contained, put newNote at next position
            newNote.setTime(where.getTime());
            // synchronize times to keep order
            eTreeList.add(i + 1, newNote);
            // everything fine, exit...
        }
    }

    /**
     * Inserts the given EventNote at the first position in the event-list. The
     * Event encapsulated in that EventNote will probably be the next event to be
     * processed by the scheduler (unless some other calls to this method are made
     * before). Note that for consistency the time of the new event-note is set to
     * the time of the next entry, if the time of the next entry is earlier.
     *
     * @param newNote EventNote : The event-note to be inserted at the first
     *                position in the event-list.
     */
    @Override
    void insertAsFirst(EventNote newNote) {

        if (!isEmpty()) { // if notes in EventList, ensure time is not later than second node
            TimeInstant next = ((EventNote) eTreeList.get(0)).getTime();
            if (TimeInstant.isBefore(next, newNote.getTime())) {
                newNote.setTime(next);
            }
        }

        eTreeList.add(0, newNote);

        // code for adding EventNote to all possible entities

        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }

        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }

        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }

        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }

    }

    /**
     * Inserts a new event-note before another EventNote specified. Note that this
     * could disturb the temporal order of the event-list. So this method should
     * only be used carefully. Note also, that EventNote 'where' must be contained
     * in the event-list or otherwise an exception will be thrown.
     *
     * @param where   EventNote : The event-note containing the event before which
     *                the newNote is supposed to be inserted into the event-list.
     * @param newNote EventNote : The new event-note to be inserted before the
     *                specified EventNote in the event-list
     * @throws SimAbortedException : if referred EventNote is not contained in the
     *                             event-list
     */
    @Override
    void insertBefore(EventNote where, EventNote newNote) {

        // code for adding EventNote to all possible entities

        Entity who1 = newNote.getEntity1();
        if (who1 != null) {
            who1.addEventNote(newNote);
        }

        Entity who2 = newNote.getEntity2();
        if (who2 != null) {
            who2.addEventNote(newNote);
        }

        Entity who3 = newNote.getEntity3();
        if (who3 != null) {
            who3.addEventNote(newNote);
        }

        EventAbstract Event = newNote.getEvent();
        if (Event != null) {
            Event.addEventNote(newNote);
        }

        int i = eTreeList.indexOf(where);

        if (i < 0) {
            Model mBuffer = null; // buffer current model
            if (newNote.getEntity1() != null) {
                mBuffer = newNote.getEntity1().getModel();
            }
            if (newNote.getEvent() != null) {
                mBuffer = newNote.getEvent().getModel();
            }
            throw new SimAbortedException(new ErrorMessage(mBuffer,
                                                           "Can not insert new event-note before given EventNote! "
                                                           + "Simulation aborted",
                                                           "Internal DESMO-J class : EventTreeList Method : "
                                                           + "insertBefore(EventNote where, EventNote newNote)",
                                                           "The event-note to insert the new note before is not contained "
                                                           + "in the event tree list.",
                                                           "This is a fatal error. Contact DESMOJ support",
                                                           newNote.getTime()));
        } else {
            newNote.setTime(where.getTime());
            // synchronize times to keep order
            eTreeList.add(i, newNote);
            // insert newN. & push afterN. one up

            // code for adding EventNote to Entity
            // variable was not used
            // Entity who = newNote.getEntity1();
            // variable was not used
            // Object o = who;
        }

    }

    /**
     * Tests if there are any scheduled events contained in the event-list. If the
     * event-list happens to be empty during the run of a simulation, this is a
     * criterium to stop the simulation, since no further action is scheduled.
     *
     * @return boolean : True if there are no Event notes contained in the
     *         event-list, false otherwise.
     */
    @Override
    boolean isEmpty() {

        return eTreeList.isEmpty();
        // simply pass the call through to the tree list.

    }

    /**
     * Returns the last EventNote in the event-list. If the event-list is empty,
     * <code>null</code> will be returned.
     *
     * @return EventNote : the last EventNote in the event-list, null if the
     *         event-list is empty
     */
    @Override
    EventNote lastNote() {

        if (isEmpty())
            return null; // Nothing here, nothign to return...
        else
            return (EventNote) eTreeList.get(eTreeList.size() - 1);
        // return last

    }

    /**
     * Returns the next event-note in the event-list relative to the given
     * EventNote. If the given EventNote is not contained in the event-list or
     * happens to be the last EventNote in the event-list, null will be returned.
     *
     * @return EventNote : The event-note following the given EventNote or
     *         <ocde>null</code> if the given EventNote was last or not found
     * @param origin EventNote : The event-note whose successor is wanted
     */
    @Override
    EventNote nextNote(EventNote origin) {

        if (eTreeList.contains(origin)) {
            if (origin == eTreeList.get(eTreeList.size() - 1)) {
                return null;
            } else
                return (EventNote) eTreeList.get(eTreeList.indexOf(origin) + 1);
        }
        return null;

    }

    /**
     * Returns the previous EventNote in the event-list relative to the given
     * EventNote. If the given EventNote is not contained in the event-list or
     * happens to be the first event-note in the event-list, null will be returned.
     *
     * @return EventNote : The event-note following the given EventNote or
     *         <ocde>null</code> if the given EventNote was first or not found
     * @param origin EventNote : The event-note whose predecessor is wanted
     */
    @Override
    EventNote prevNote(EventNote origin) {

        if (eTreeList.contains(origin)) {
            if (origin == eTreeList.get(0)) {
                return null;
            }
            return (EventNote) eTreeList.get(eTreeList.indexOf(origin) - 1);
        }
        return null;

    }

    /**
     * Removes the given EventNote from the event-list.
     *
     * Warning: Make sure to tell the entity of the event-note to delete the Note
     * from its List as well.
     *
     * Warning: Make sure to tell the entity of the event-note to delete the Note
     * from its List as well.
     *
     * @param note EventNote : The event-note to be removed from the event-list
     */
    @Override
    void remove(EventNote note) {

        int pos = eTreeList.indexOf(note);

        if (pos == -1)
            return; // do nothing if it doesn't exist
        else {
            eTreeList.remove(pos); // go ahead and crunch it!

            if (note.getEntity1() != null) // if an entity exists (no external event)
            {
                note.getEntity1().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEntity2() != null) // if an entity exists (no external event)
            {
                note.getEntity2().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEntity3() != null) // if an entity exists (no external event)
            {
                note.getEntity3().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEvent() != null) // if an event exists
            {
                note.getEvent().removeEventNote(note); // remove EventNote
            }
        }

    }

    /**
     * Removes the first event-note from the event-list. Does nothing if the
     * event-list is already empty.
     */
    @Override
    EventNote removeFirst() {

        if (!eTreeList.isEmpty()) {
            EventNote note = (EventNote) eTreeList.remove(0);

            if (note.getEntity1() != null) // if an entity exists (no external event)
            {
                note.getEntity1().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEntity2() != null) // if an entity exists (no external event)
            {
                note.getEntity2().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEntity3() != null) // if an entity exists (no external event)
            {
                note.getEntity3().removeEventNote(note); // removes list entry in Entity!
            }

            if (note.getEvent() != null) // if an event exists
            {
                note.getEvent().removeEventNote(note); // remove EventNote
            }
            return note;
        }
        return null;
    }

}
