package desmoj.core.simulator;

/**
 * This interface describes the set of methods an event-list carrying the
 * scheduled events and entities in a temporal order has to implement. This is a
 * part of the framework that does not have to be implemented by the user in
 * order to get a simulation running, since the scheduler already uses the class
 * EventVector as default EventList construction. Since each step in the
 * discrete simulation requires searching and manipulating the event-list, this
 * is probably one of the best places in the framework to optimize execution
 * performance. Especially if special models show specific behaviour i.e.
 * primarily inserting new events at the very end of the event-list, other
 * implementations of the event-list might support faster access times.
 *
 * @see EventVectorList
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public abstract class EventList {

    /**
     * Returns the first event-note in the event-list. That EventNote contains
     * information about the next event to happen and will be the next event
     * processed by the scheduler.
     *
     * @return EventNote : The first event-note in the event-list
     */
    abstract EventNote firstNote();

    /**
     * Inserts a new event-note into the event-list. Event notes are sorted by the
     * point if time they are scheduled to happen. This method ensures that the new
     * event-note is inserted in the right place in the event-list, scanning through
     * the list whenever a new event-note has to be inserted.
     *
     * @param newNote EventNote : The event-note to be sorted into the event-list
     */
    abstract void insert(EventNote newNote);

    /**
     * Inserts the new event-note straight after the other specified EventNote. This
     * other EventNote must already be scheduled. Otherwise a warning will be issued
     * and the method returns without changing the event-list.
     *
     * @param afterNote EventNote : The event-note already scheduled, that the new
     *                  EventNote is supposed to be inserted after.
     * @param newNote   EventNote : The new event-note to be inserted before the
     *                  specified EventNote
     */
    abstract void insertAfter(EventNote afterNote, EventNote newNote);

    /**
     * Inserts the given EventNote at the first position in the event-list. The
     * Event encapsulated in that EventNote will probably be the next event to be
     * processed by the scheduler (unless some other calls to this method are made
     * before). Note that this operation changes the scheduled time of the given
     * EventNote to the actual simulation time to keep the temporal order of the
     * event-list.
     *
     * @param newNote EventNote : The event-note to be inserted at the first
     *                position in the event-list
     */
    abstract void insertAsFirst(EventNote newNote);

    /**
     * Inserts the new event-note straight before the other specified EventNote.
     * This other EventNote must already be scheduled. Otherwise the position to
     * insert the given EventNote can not be determined, resulting in a
     * ItemNotScheduledException being thrown.
     *
     * @param beforeNote EventNote : The event-note already scheduled, that the new
     *                   EventNote is supposed to be inserted before.
     * @param newNote    EventNote : The new event-note to be inserted before the
     *                   specified EventNote
     */
    abstract void insertBefore(EventNote beforeNote, EventNote newNote);

    /**
     * Tests if there are any scheduled events contained in the event-list. If the
     * event-list happens to be empty during the run of a simulation, this is a
     * criterium to stop the simulation, since no further action is scheduled.
     *
     * @return boolean : Is <code>true</code> if there are no EventNote contained in
     *         the event-list, <code>false</code> otherwise.
     */
    abstract boolean isEmpty();

    /**
     * Returns if the event-list processes concurrent Events in random order or not.
     *
     * @return boolean: <code>true</code> if concurrent Events are randomized,
     *         <code>false</code> otherwise
     * @author Ruth Meyer
     */
    abstract boolean isRandomizingConcurrentEvents();

    /**
     * Returns the last EventNote in the event-list.
     *
     * @return EventNote : The last EventNote in the event-list
     */
    abstract EventNote lastNote();

    /**
     * Returns the next event-note in the event-list relative to the given
     * EventNote. If the given EventNote is not contained in the event-list or
     * happens to be the last EventNote in the event-list, <code>null</code> will be
     * returned.
     *
     * @return EventNote : The event-note following the given EventNote or
     *         <ocde>null</code> if the given EventNote was last or not found
     * @param origin EventNote : The event-note whose successor is wanted
     */
    abstract EventNote nextNote(EventNote origin);

    /**
     * Returns the previous EventNote in the event-list relative to the given
     * EventNote. If the given EventNote is not contained in the event-list or
     * happens to be the first event-note in the evnet-list, <code>null</code> will
     * be returned.
     *
     * @return EventNote : The event-note following the given EventNote or
     *         <ocde>null</code> if the given EventNote was first or not found
     * @param origin EventNote : The event-note whose predecessor is wanted
     */
    abstract EventNote prevNote(EventNote origin);

    /**
     * Removes the given EventNote from the event-list.
     *
     * @param note EventNote . The event-note to be removed from the event-list
     */
    abstract void remove(EventNote note);

    /**
     * Removes the first event-note from the event-list.
     *
     * @return The removed note, or null if the list was empty.
     */
    abstract EventNote removeFirst();
}
