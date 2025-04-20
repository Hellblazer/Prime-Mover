package desmoj.core.simulator;

import java.util.Map.Entry;
import java.util.TreeMap;

import desmoj.core.exception.SimAbortedException;
import desmoj.core.report.ErrorMessage;

/**
 * An {@link EventList} implementation that is based on a sorted map (TreeMap).
 * The map's keys are time+priority (using the fact that EventNote.compareTo is
 * only based on these two fields). Every value is a singly linked list with all
 * {@link EventNote}s having the same time and priority, in order.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tobias Baum
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
public class SortedMapEventList extends EventList {

    /**
     * Wrapper around an {@link EventNote} and node in a linked list at the same
     * time.
     */
    private static final class EventNoteWrapper {

        private EventNoteWrapper next;
        private final EventNote  note;

        public EventNoteWrapper(EventNote newNote) {
            this.note = newNote;
        }

        public EventNoteWrapper(EventNote newNote, EventNoteWrapper next) {
            assert next == null || newNote.compareTo(next.note) == 0;
            this.note = newNote;
            this.next = next;
        }

        /**
         * Add the given {@link EventNote} to the end of the linked list.
         */
        public void addAtEnd(EventNote newNote) {

//          if (next == null) {
//              assert this.note.compareTo(newNote) == 0;
//              next = new EventNoteWrapper(newNote);
//          } else {
//              next.addAtEnd(newNote);
//          }

            EventNoteWrapper w = this;
            while (w.next != null)
                w = w.next;
            assert w.note.compareTo(newNote) == 0;
            w.next = new EventNoteWrapper(newNote);

        }

        /**
         * Return the event note wrapper that lies before the wrapper with the given
         * {@link EventNote} in the linked list.
         *
         * @return The found node, or null if the note is not contained or is the first
         *         in the list.
         */
        public EventNoteWrapper findWrapperBefore(EventNote beforeNote) {
            if (this.next != null) {
                if (this.next.note == beforeNote) {
                    return this;
                } else {
                    return this.next.findWrapperBefore(beforeNote);
                }
            }
            return null;
        }

        /**
         * Return the event note wrapper that contains the given {@link EventNote} in
         * the linked list.
         *
         * @return The found node, or null if the note is not contained.
         */
        public EventNoteWrapper findWrapperWith(EventNote noteToFind) {
            if (this.note == noteToFind) {
                return this;
            } else if (this.next != null) {
                return this.next.findWrapperWith(noteToFind);
            } else {
                return null;
            }
        }

        /**
         * Returns the last note in the linked list.
         */
        public EventNote getLastNote() {
            return this.next == null ? this.note : this.next.getLastNote();
        }

        /**
         * Insert the given {@link EventNote} after this node in the linked list.
         */
        public void insertSucc(EventNote newNote) {
            this.next = new EventNoteWrapper(newNote, this.next);
        }

        /**
         * Remove the successor of this node from the linked list.
         */
        public void removeSucc() {
            this.next = this.next.next;
        }

    }

    private TreeMap<EventNote, EventNoteWrapper> queue = new TreeMap<>();

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
     * {@inheritDoc}.
     */
    @Override
    EventNote firstNote() {
        if (queue.isEmpty()) {
            return null;
        }
        // the first note is the first entry in the linked list for the smallest key
        return queue.firstEntry().getValue().note;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    void insert(EventNote newNote) {
        addNoteToEntities(newNote);

        EventNoteWrapper old = queue.get(newNote);
        if (old == null) {
            // there was no note with the same key => create a new entry
            queue.put(newNote, new EventNoteWrapper(newNote));
        } else {
            // there already was a note with the same key => add it to the end of its list
            old.addAtEnd(newNote);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    void insertAfter(EventNote afterNote, EventNote newNote) {
        EventNoteWrapper reference = queue.get(afterNote);
        if (reference == null) {
            // there is not even a note with the same key as the reference
            throw referenceNotFoundException(newNote, "insertAfter");
        }
        EventNoteWrapper pred = reference.findWrapperWith(afterNote);
        if (pred == null) {
            // the reference note was not found
            throw referenceNotFoundException(newNote, "insertAfter");
        }
        // insert into the list as successor to afterNote and ensure correct time
        correctTimeAndPriority(newNote, afterNote);
        pred.insertSucc(newNote);
        addNoteToEntities(newNote);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    void insertAsFirst(EventNote newNote) {
        if (queue.isEmpty()) {
            // there are no entries => just insert
            queue.put(newNote, new EventNoteWrapper(newNote));
            addNoteToEntities(newNote);
            return;
        }

        // determine the smallest key
        EventNoteWrapper oldFirst = this.queue.firstEntry().getValue();
        int priorityCmp = newNote.compareTo(oldFirst.note);
        if (priorityCmp < 0) {
            // new note has smaller key => insert new entry
            queue.put(newNote, new EventNoteWrapper(newNote));
        } else {
            // new note has larger or same key => assure time is consistent and add at front
            // of list
            if (priorityCmp > 0) {
                correctTimeAndPriority(newNote, oldFirst.note);
            }
            queue.replace(oldFirst.note, new EventNoteWrapper(newNote, oldFirst));
        }
        addNoteToEntities(newNote);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    void insertBefore(EventNote beforeNote, EventNote newNote) {
        EventNoteWrapper reference = queue.get(beforeNote);
        if (reference == null) {
            // there is not even a note with the same key as the reference
            throw referenceNotFoundException(newNote, "insertBefore");
        }
        if (reference.note == beforeNote) {
            // reference is the first in the key's list => add to front of list
            correctTimeAndPriority(newNote, beforeNote);
            queue.replace(newNote, new EventNoteWrapper(newNote, reference));
        } else {
            // reference is not the first => search in the rest of the list
            EventNoteWrapper pred = reference.findWrapperBefore(beforeNote);
            if (pred == null) {
                // not contained
                throw referenceNotFoundException(newNote, "insertBefore");
            }
            // reference is contained in list => assure that time is equal and insert before
            // the reference
            correctTimeAndPriority(newNote, beforeNote);
            pred.insertSucc(newNote);
        }
        addNoteToEntities(newNote);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    boolean isEmpty() {
        return this.queue.isEmpty();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    EventNote lastNote() {
        if (queue.isEmpty()) {
            return null;
        }
        // last note is the last entry of the list with the largest key
        return queue.lastEntry().getValue().getLastNote();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    EventNote nextNote(EventNote origin) {
        EventNoteWrapper reference = queue.get(origin);
        if (reference == null) {
            // there is not even a note with the same key as origin
            return null;
        }
        EventNoteWrapper pred = reference.findWrapperWith(origin);
        if (pred == null) {
            // origin is not contained
            return null;
        }
        if (pred.next == null) {
            // there are no more entries for the current time+priority, get the first note
            // from the next key
            Entry<EventNote, EventNoteWrapper> nextEntry = queue.higherEntry(origin);
            if (nextEntry == null) {
                return null;
            }
            return nextEntry.getValue().note;
        } else {
            // return the note after origin's node
            return pred.next.note;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    EventNote prevNote(EventNote origin) {
        EventNoteWrapper reference = queue.get(origin);
        if (reference == null) {
            // there is not even a note with the same key as origin
            return null;
        }
        if (reference.note == origin) {
            // there is no previous entry for the current time+priority, get the first note
            // from the previous key
            Entry<EventNote, EventNoteWrapper> prevEntry = queue.lowerEntry(origin);
            if (prevEntry == null) {
                return null;
            }
            return prevEntry.getValue().getLastNote();
        } else {
            // find the wrapper before the node with origin
            EventNoteWrapper pred = reference.findWrapperBefore(origin);
            if (pred == null) {
                // note is not contained in the list
                return null;
            }
            return pred.note;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    void remove(EventNote note) {
        EventNoteWrapper wrapper = queue.get(note);
        if (wrapper == null) {
            // there is not even a note with the same key as origin
            return;
        }
        if (wrapper.note == note) {
            // note is the first in the list => remove in from the list and replace/remove
            // the key's entry
            if (wrapper.next != null) {
                queue.replace(note, wrapper.next);
            } else {
                queue.remove(note);
            }
        } else {
            // note could be somewhere else in the list => remove it from there
            EventNoteWrapper pred = wrapper.findWrapperBefore(note);
            if (pred == null) {
                return;
            }
            pred.removeSucc();
        }
        removeNoteFromEntities(note);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    EventNote removeFirst() {
        if (queue.isEmpty()) {
            // no entries => nothing to remove
            return null;
        }
        // determine the wrapper with the smallest key
        EventNoteWrapper first = queue.firstEntry().getValue();
        if (first.next != null) {
            // there are several notes with that key => remove the first from the list
            queue.replace(first.note, first.next);
        } else {
            // no entries with that key left => remove the list from the map
            queue.remove(first.note);
        }
        removeNoteFromEntities(first.note);
        return first.note;
    }

    /**
     * Add the {@link EventNote} to its various entities.
     */
    private void addNoteToEntities(EventNote newNote) {
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

    private void correctTimeAndPriority(EventNote newNote, EventNote oldFirst) {
        newNote.setTime(oldFirst.getTime());
        newNote.setPriority(oldFirst.getPriority());
    }

    /**
     * Creates (but does not throw) an exception saying that an {@link EventNote}
     * that should be used as a reference point could not be found.
     */
    private SimAbortedException referenceNotFoundException(EventNote newNote, String method) {
        Model mBuffer = null; // buffer current model
        if (newNote.getEntity1() != null) {
            mBuffer = newNote.getEntity1().getModel();
        }
        if (newNote.getEvent() != null) {
            mBuffer = newNote.getEvent().getModel();
        }
        return new SimAbortedException(new ErrorMessage(mBuffer,
                                                        "Can not insert new event-note with reference to given EventNote! "
                                                        + "Simulation aborted",
                                                        "Internal DESMO-J class : SortedMapEventList Method : "
                                                        + method,
                                                        "The reference event-note is not contained in the event tree list.",
                                                        "This is a fatal error. Contact DESMOJ support",
                                                        newNote.getTime()));
    }

    /**
     * Remove the {@link EventNote} from its various entities.
     */
    private void removeNoteFromEntities(EventNote note) {
        if (note.getEntity1() != null) { // if an entity exists (no external event)
            note.getEntity1().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEntity2() != null) { // if an entity exists (no external event)
            note.getEntity2().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEntity3() != null) { // if an entity exists (no external event)
            note.getEntity3().removeEventNote(note); // removes list entry in Entity!
        }

        if (note.getEvent() != null) { // if an event exists
            note.getEvent().removeEventNote(note); // remove EventNote
        }
    }
}
