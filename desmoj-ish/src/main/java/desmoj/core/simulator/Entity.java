package desmoj.core.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the superclass for all entities of a model. Entities are supposed to be scheduled together with a
 * compatible event at a certain point of simulation time or relative to another event in present or future simulation
 * time.
 * <p>
 * Entities typically encapsulate all information about a model entity relevant to the modeller. Events can manipulate
 * these informations when the scheduled point of simulation time is reached and thus change the state of the model.
 * When modelling different types of entities you need to derive different classes from this superclass. Each carrying
 * the specific information to represent its counterpart in the system modelled. Thus a simulation of e.g. a factory
 * would require both machines and material to be subclasses of class
 * <code>Entity</code>. They can act on each other by scheduling themselves or
 * other Entities with the appropriate events. To use more than one entity of one type, create multiple instances of the
 * same <code>Entity</code> class. For better identification, all instances created from a subclass of class
 * <code>NamedObject</code> (just as <code>Entity</code> is) get an individual
 * identification number as a suffix to their name so there is no need to name each individual differently yourself.
 * <p>
 * Entities can carry a queuing priority that can be modified after the entity has been instantiated, applied for
 * inserting Entities into any kind of Queues: The entity's priority determines it's position inside the queue on
 * entering it. Although within a model all attributes of an entity could be made public it is advisable to support data
 * hiding by providing methods for accessing the internal attributes, as always in oo-design.
 *
 * @author Tim Lechler, modified by Justin Neumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see Event
 * @see SimProcess
 * @see NamedObject
 */
public abstract class Entity extends Schedulable {

    /**
     * The identfication number of this Entity.
     */
    private long _identNumber;
    /**
     * The <code>Queue</code>s or other <code>QueueBased</code> objects where this Entity is queued.
     */
    private ArrayList<QueueBased> _myQueues;
    /**
     * The queuing priority of the entity.
     */
    private int _myQueuingPriority;

    public Entity(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace); // create Schedulable
        _myQueuingPriority = 0; // default queuing priority
        _myQueues = new ArrayList<>();

        // creates and registers identifier
        _identNumber = owner.linkWithIdentNumber(this);

    }

    /**
     * Checks if the two entities have the same priority. Note that this is a static method available through calling
     * the entity's class i.e.
     * <code>Entity.isEqual(a,b)</code> where <code>a</code> and <code>b</code> are
     * valid Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has the same priority as
     * <code>b</code>,<code>/false</code> otherwise
     */
    public final static boolean isEqual(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        return (a.getQueueingPriority() == b.getQueueingPriority());

    }

    /**
     * Checks if the first of the two entities has a higher priority than the second. Note that this is a static method
     * available through calling the entity's class i.e. <code>Entity.isLarger(a,b)</code> where a and b are valid
     * Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has a larger priority than
     * <code>b</code>,<code>/false</code> otherwise
     */
    public final static boolean isLarger(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        return (a.getQueueingPriority() > b.getQueueingPriority());

    }

    /**
     * Checks if the first of the two entities has higher or same priority than the second. Note that this is a static
     * method available through calling the entity's class i.e. <code>Entity.isLargerOrEqual(a,b)</code> where a and b
     * are valid Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has a larger or equal priority than
     * <code>b</code>,<code>/false</code> otherwise
     */
    public final static boolean isLargerOrEqual(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        return (a.getQueueingPriority() >= b.getQueueingPriority());

    }

    /**
     * Checks if the first of the two entities have different priorities. Note that this is a static method available
     * through calling the entity's class i.e.
     * <code>Entity.isNotEqual(a,b)</code> where a and b are valid Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has a different priority than
     * <code>b</code>,<code>/false</code> otherwise
     */
    public final static boolean isNotEqual(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        return (a.getQueueingPriority() != b.getQueueingPriority());

    }

    /**
     * Checks if the first of the two entities has a lower priority than the second. Note that this is a static method
     * available through calling the entity's class i.e. <code>Entity.isSmaller(a,b)</code> where a and b are valid
     * Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has a lower priority than
     * <code>b</code>,<code>/false</code> otherwise
     */
    public final static boolean isSmaller(Entity a, Entity b) {
        if (a == null || b == null) {
            return false;
        }
        return (a.getQueueingPriority() < b.getQueueingPriority());

    }

    /**
     * Checks if the first of the two entities has lower or same priority than the second. Note that this is a static
     * method available through calling the entity's class i.e. <code>Entity.isSmallerOrEqual(a,b)</code> where a and b
     * are valid Entity objects.
     *
     * @param a Entity : First comparand entity
     * @param b Entity : Second comparand entity
     * @return boolean : Is <code>true</code> if <code>a</code> has a smaller or equal priority than <code>b</code>,
     * <code>/false</code> otherwise
     */
    public final static boolean isSmallerOrEqual(Entity a, Entity b) {

        if (a == null || b == null) {
            return false;
        }

        return (a.getQueueingPriority() <= b.getQueueingPriority());

    }

    /**
     * Returns the entity's identification number.
     *
     * @return Long : The entity's identification number
     */
    public long getIdentNumber() {

        return _identNumber; // returns the identification number

    }

    /**
     * Returns the entity's queuing priority. Default priority of an entity is zero. Higher priorities are positive,
     * lower priorities negative.
     *
     * @return int : The entity's priority
     */
    public int getQueueingPriority() {

        return _myQueuingPriority;

    }

    /**
     * Returns a list of queues and other <code>QueueBased</code> objects where this
     * <code>Entity</code> is queued.
     *
     * @return List<QueueBased> : The <code>QueueBased</code>s containing this entity; may be empty if entity is not
     * queued.
     */
    public List<QueueBased> getQueues() {
        return new ArrayList<>(this._myQueues);
    }

    /**
     * Returns a list of events associated to this Entity object. If the Entity object is not currently scheduled, an
     * empty list will be returned. Remind that all different Event classes can be included.
     *
     * @return List<EventAbstract> : The events associated to the entity
     */
    public List<EventAbstract> getScheduledEvents() {
        List<EventAbstract> list = new LinkedList<>();
        for (EventNote note : _schedule) {
            list.add(note.getEvent());
        }
        return list;

    }

    /**
     * Tests if this <code>Entity</code> queued in a at least one queue or other
     * <code>QueueBased</code>.
     *
     * @return boolean : Is <code>true</code> if this Entity is queued in at least one queue or other
     * <code>QueueBased</code>, <code>false</code>
     * otherwise.
     */
    public boolean isQueued() {
        return !this._myQueues.isEmpty();
    }

    /**
     * Tests if this Entity actually is a SimProcess. Although SimProcess have an individual life-cycle, they can also
     * be handled like entities and be scheduled to be manipulated by an event.
     *
     * @return boolean : Is <code>true</code> if this Entity is an instance of class
     * <code>SimProcess</code>,<code>false</code> otherwise
     */
    public boolean isSimProcess() {

        return (this instanceof SimProcess);

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf2Entities at the current time. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what EventOf2Entities : The event to be scheduled
     * @see SimClock
     */
    public <E extends Entity> void schedule(E who2, EventOf2Entities<?, E> what) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what) {",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what) {",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what) {",
                        "The EventOf2Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, null, null, presentTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, what, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf2Entities at the given point of time. Method returns
     * with a warning message if either Entity or Event are already scheduled in the event-list. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what EventOf2Entities : The event to be scheduled
     * @param when TimeInstant : The point in simulation time this event is scheduled to happen.
     * @see SimClock
     */
    public <E extends Entity> void schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((when == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The EventOf2Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, null, null, when, null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, what, when);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf2Entities at the current time plus the given offset.
     * No preemption, i.e. a process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what EventOf2Entities : The event to be scheduled
     * @param dt   TimeSpan : The offset to the current simulation time at which the event is to be scheduled
     * @see SimClock
     */
    public <E extends Entity> void schedule(E who2, EventOf2Entities<?, E> what, TimeSpan dt) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeSpan dt) {",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((dt == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName() + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeSpan dt) {",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeSpan dt) {",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeSpan dt) {",
                        "The EventOf2Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, null, null, TimeOperations.add(presentTime(), dt), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, what, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf3Entities at the current time. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3 Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what EventOf3Entities : The event to be scheduled
     * @see SimClock
     */
    public <E extends Entity, F extends Entity> void schedule(E who2, F who3, EventOf3Entities<?, E, F> what) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what)",
                        "The EventOf3Entities given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what)",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, null, null, presentTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, who3, what, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf3Entities at the given point of time. No preemption,
     * i.e. a process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3 Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what EventOf3Entities : The event to be scheduled
     * @param when TimeInstant : The point in simulation time the event is scheduled to happen.
     * @see SimClock
     */
    public <E extends Entity, F extends Entity> void schedule(E who2, F who3, EventOf3Entities<?, E, F> what,
                                                              TimeInstant when) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity,F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeInstant when) {",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity,F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeInstant when) {",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((when == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity,F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeInstant when) {",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity,F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeInstant when) {",
                        "The EventOf3Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf3Entities reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity,F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeInstant when) {",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, null, null, when, null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, who3, what, when);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given EventOf3Entities at the current time plus the given offset.
     * No preemption, i.e. a process calling this method will continue until passivated or hold.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3 Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what EventOf3Entities : The event to be scheduled
     * @param dt   TimeSpan : The offset to the current simulation time at which the event is to be scheduled
     * @see SimClock
     */
    public <E extends Entity, F extends Entity> void schedule(E who2, F who3, EventOf3Entities<?, E, F> what,
                                                              TimeSpan dt) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((dt == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The EventOf3Entities given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, null, null, TimeOperations.add(presentTime(), dt), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, who2, who3, what, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given Event at the current time. Method returns with a warning
     * message if either Entity or Event are already scheduled in the EventList. No preemption, i.e. a process calling
     * this method will continue until passivated or hold.
     *
     * @param what Event : The Event that manipulates this Entity
     * @see SimClock
     */
    public void schedule(Event<?> what) {

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: schedule(Event what)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: schedule(Event what)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, null, null, presentTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, what, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given Event at the given point of time. Method returns with a
     * warning message if either Entity or Event are already scheduled in the event-list. No preemption, i.e. a process
     * calling this method will continue until passivated or hold.
     *
     * @param what Event : The Event that manipulates this Entity
     * @param when TimeInstant : The point in simulation time this event is scheduled to happen.
     * @see SimClock
     */
    public void schedule(Event<?> what, TimeInstant when) {
        if ((when == null)) {
            sendWarning("Can't schedule Entity!",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeInstant when)",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeInstant when)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeInstant when)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, null, null, when, null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, what, when);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity to be manipulated by the given Event at the current time plus the given offset. Method
     * returns with a warning message if either Entity or Event are already scheduled in the EventList. No preemption,
     * i.e. a process calling this method will continue until passivated or hold.
     *
     * @param what Event : The Event that manipulates this Entity
     * @param dt   TimeSpan : The offset to the current simulation time at which the event is to be scheduled
     * @see SimClock
     */
    public void schedule(Event<?> what, TimeSpan dt) {
        if ((dt == null)) {
            sendWarning("Can't schedule Entity!",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeSpan dt)",
                        "The simulation time given as parameter is a null reference.",
                        "Be sure to have a valid simulation time reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeSpan dt)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeSpan dt)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, null, null, TimeOperations.add(presentTime(), dt), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, what, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this Entity with the given Event to occur directly after the given Schedulable that is already
     * scheduled. Note that this event's point of simulation time will be set to be the same as the Schedulable's time.
     * Thus the event will occur directly after the given Schedulable but the simulation clock will not change. Will
     * return with a warning message if the Schedulable given as parameter is not scheduled. If there are multiple
     * schedules for the given Schedulable, the event will be scheduled after the last occurrence.
     *
     * @param after Schedulable : The Schedulable this Entity should be scheduled after
     * @param what  Event : The Event to manipulate this Entity
     */
    public void scheduleAfter(Schedulable after, Event<?> what) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, Event what)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((after == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, Event what)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, Event what)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, Event what)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        if (currentlySendTraceNotes()) {
            if (this == currentEntity()) {
                sendTraceNote("schedules '" + what.getName() + "' of itself after '" + after.getName() + "' at "
                              + after.getEventNotes().get(after.getEventNotes().size() - 1).getTime().toString());
            } else {
                sendTraceNote(
                "schedules '" + what.getName() + "' of '" + getName() + "' after '" + after.getName() + "' at "
                + after.getEventNotes().get(after.getEventNotes().size() - 1).getTime().toString());
            }
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, after, null,
                                        after.getEventNotes().get(after.getEventNotes().size() - 1).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleAfter(after, this, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleAfter " + after.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                     .getScheduler()
                                                                                                     .toString());
        }

    }

    /**
     * Schedules this Entity with the given EventOf2Entities to occur directly after the given Schedulable that is
     * already scheduled. Note that the event's point of simulation time will be set to be the same as the Schedulable's
     * time. Thus the event will occur directly after the given Schedulable but the simulation clock will not change.
     * Will return with a warning message if the Schedulable given as parameter is not scheduled. If there are multiple
     * schedules for the given Schedulable, the event will be scheduled after the last occurrence.
     *
     * @param who2  Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what  EventOf2Entities : The event to be scheduled
     * @param after Schedulable : The Schedulable the event should be scheduled after
     */
    public <E extends Entity> void scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2)",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((after == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> scheduleAfter(Schedulable after, EventOf2Entities<?, E> what, E who2)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, after, null,
                                        after.getEventNotes().get(after.getEventNotes().size() - 1).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleAfter(after, this, who2, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleAfter " + after.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                     .getScheduler()
                                                                                                     .toString());
        }

    }

    /**
     * Schedules this Entity with the given EventOf3Entities to occur directly after the given Schedulable that is
     * already scheduled. Note that the event's point of simulation time will be set to be the same as the Schedulable's
     * time. Thus the event will occur directly after the given Schedulable but the simulation clock will not change.
     * Will return with a warning message if the Schedulable given as parameter is not scheduled. If there are multiple
     * schedules for the given Schedulable, the event will be scheduled after the last occurrence.
     *
     * @param who2  Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3  Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what  EventOf3Entities : The event to be scheduled
     * @param after Schedulable : The Schedulable this Entity should be scheduled after
     * @param what  Event : The Event to manipulate this Entity
     */
    public <E extends Entity, F extends Entity> void scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what,
                                                                   E who2, F who3) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The EventOf3Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf3Entities reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((after == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> scheduleAfter(Schedulable after, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, after, null,
                                        after.getEventNotes().get(after.getEventNotes().size() - 1).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleAfter(after, this, who2, who3, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleAfter " + after.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                     .getScheduler()
                                                                                                     .toString());
        }

    }

    /**
     * Schedules this Entity with the given Event to occur directly before the given Schedulable that is scheduled. Note
     * that this event's point of simulation time will be set to be the same as the Schedulable's time. Thus the event
     * will occur directly before the given Schedulable but the simulation clock will not change. Issues a warning
     * message if the Schedulable given is not scheduled. If there are multiple schedules for the given Schedulable, the
     * event will be scheduled before the first occurrence.
     *
     * @param before Schedulable : The Schedulable this Entity should be scheduled before
     * @param what   Event : The Event to manipulate this Entity
     */
    public void scheduleBefore(Schedulable before, Event<?> what) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleBefore(Schedulable before, Event what)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((before == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleBefore(Schedulable before, Event what)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: scheduleBefore(Schedulable before, Event what)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: scheduleBefore(Schedulable before, Event what)",
                        "The Event to be scheduled with thisEntity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, null, before, before.getEventNotes().get(0).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleBefore(before, this, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                       .getScheduler()
                                                                                                       .toString());
        }

    }

    /**
     * Schedules this Entity with the given EventOf2Entities to occur directly before the given Schedulable that is
     * scheduled. Note that the event's point of simulation time will be set to be the same as the Schedulable's time.
     * Thus the event will occur directly before the given Schedulable but the simulation clock will not change. Issues
     * a warning message if the Schedulable given is not scheduled. If there are multiple schedules for the given
     * Schedulable, the event will be scheduled before the first occurrence.
     *
     * @param who2   Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what   EventOf2Entities : The event to be scheduled
     * @param before Schedulable : The Schedulable this Entity should be scheduled before
     */
    public <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2)",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((before == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> void scheduleBefore(Schedulable before, EventOf2Entities<?, E> what, E who2)",
                        "The EventOf2Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, null, before, before.getEventNotes().get(0).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleBefore(before, this, who2, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                       .getScheduler()
                                                                                                       .toString());
        }

    }

    /**
     * Schedules this Entity with the given EventOf3Entities to occur directly before the given Schedulable that is
     * scheduled. Note that the event's point of simulation time will be set to be the same as the Schedulable's time.
     * Thus the event will occur directly before the given Schedulable but the simulation clock will not change. Issues
     * a warning message if the Schedulable given is not scheduled. If there are multiple schedules for the given
     * Schedulable, the event will be scheduled before the first occurrence.
     *
     * @param who2   Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3   Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what   EventOf3Entities : The event to be scheduled
     * @param before Schedulable : The Schedulable this Entity should be scheduled before
     */
    public <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what,
                                                                    E who2, F who3) {

        // check parameters
        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((before == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Entity  'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this " + "Entity to be scheduled with.");
            return; // no proper parameter
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The Schedulable given as parameter is not scheduled, "
                        + "thus no position can be determined for this Entity.",
                        "Be sure that the Schedulable given as aprameter is "
                        + "actually scheduled. You can check that by calling its "
                        + "method isScheduled() which returns a boolean telling"
                        + "you whether it is scheduled or not.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> void scheduleBefore(Schedulable before, EventOf3Entities<?, E, F> what, E who2, F who3)",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, null, before, before.getEventNotes().get(0).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleBefore(before, this, who2, who3, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                       .getScheduler()
                                                                                                       .toString());
        }

    }

    /**
     * Schedules this entity with an event to be executed immediately, preempting the process lifecycle executed at the
     * moment.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf2Entities.
     * @param what EventOf2Entities : The event to be scheduled
     * @see SimClock
     */
    public <E extends Entity> void schedulePreempt(E who2, EventOf2Entities<?, E> what) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity!", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before " + "calling this method.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The EventOf2Entities given as parameter is a null reference.",
                        "Be sure to have a valid EventOf2Entities reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity> schedule(E who2, EventOf2Entities<?, E> what, TimeInstant when)",
                        "The EventOf2Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, null, null, null, presentTime(), "preempted");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(this, who2, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this entity with an event to be executed immediately, preempting the process lifecycle executed at the
     * moment.
     *
     * @param who2 Entity : The second entity to be scheduled for the EventOf3Entities.
     * @param who3 Entity : The third entity to be scheduled for the EventOf3Entities.
     * @param what EventOf3Entities : The event to be scheduled
     * @see SimClock
     */
    public <E extends Entity, F extends Entity> void schedulePreempt(E who2, F who3, EventOf3Entities<?, E, F> what) {

        if ((who2 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The EventOf3Entities given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored", "Entity : " + getName()
                        + " Method: <E extends Entity, F extends Entity> schedule(E who2, F who3, EventOf3Entities<?, E, F> what, TimeSpan dt)",
                        "The EventOf3Entities to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, who2, who3, null, null, presentTime(), "preempted");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(this, who2, who3, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this entity with an event to be executed immediately, preempting the process lifecycle executed at the
     * moment.
     *
     * @param what Event : The Event that manipulates this Entity
     * @see SimClock
     */
    public void schedulePreempt(Event<?> what) {

        if ((what == null)) {
            sendWarning("Can't schedule Entity! Command ignored.",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeSpan dt)",
                        "The Event given as parameter is a null reference.",
                        "Be sure to have a valid Event reference for this event " + "to be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(what)) // Entity and Event are part of model
        {
            sendWarning("Can't schedule Entity! Command ignored",
                        "Entity : " + getName() + " Method: schedule(Event what, TimeSpan dt)",
                        "The Event to be scheduled with this Entity is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // was already scheduled
        }

        // generate trace
        this.generateTraceForScheduling(what, null, null, null, null, presentTime(), "preempted");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(this, what);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Sets the entity's queuing priority to a given integer value. The default priority of each entity (unless assigned
     * otherwise) is zero. Negative priorities are lower, positive priorities are higher. All values should be inside
     * the range defined by Java's integral <code>integer</code> data type [-2147483648, +2147483647].
     *
     * An entity's queuing priority can be used by the modeller to determine how the entity is treated by queues, though
     * how precisely a queue will use the priority to determine sort order is up to it's queuing strategy:
     * <ul>
     * <li><code>QueueBased.FIFO</code> sorts entities by their queuing priority,
     * highest priority first. Entities with the same priority are enqueued based on
     * &quot;first in, first out&quot;.</li>
     * <li><code>QueueBased.LIFO</code> also sorts entities by their priority,
     * highest priority first. However, entities with the same priority are enqueued
     * based on &quot;last in, first out&quot;.</li>
     * <li><code>QueueBased.Random</code> assigns a random position to each entity
     * entering the queue, disregarding priority.</li>
     * </ul>
     * Of course, the modeller is free to use the queuing priority to determine how
     * entities are processed by the components he implements himself, whether they
     * are queues or not.
     *
     * @param newPriority int : The new queuing priority value
     */
    public void setQueueingPriority(int newPriority) {

        this._myQueuingPriority = newPriority;

    }

    /**
     * Creates and returns a copy of this entity. Note that subclasses have to implement the interface
     * <code>java.lang.Cloneable</code> to actually use this method as otherwise, a
     * <code>CloneNotSupportedException</code> will be thrown.
     *
     * @return Entity : A copy of this entity.
     */
    @Override
    protected Entity clone() throws CloneNotSupportedException {
        Entity c = (Entity) super.clone();
        c._myQueues = new ArrayList<>();
        c._identNumber = this.getModel().linkWithIdentNumber(c);
        return c;
    }

    /**
     * Utility method to generate trace output for scheduling this event (internal use only).
     *
     * @param Event  the event to be scheduled
     * @param who1   the second entity scheduled with the event (or
     *               <code>null</code> if not applicable)
     * @param who2   the third entity scheduled with the event (or <code>null</code> if not applicable)
     * @param after  the Schedulable after which the event is scheduled (or
     *               <code>null</code> if not applicable)
     * @param before the Schedulable before which the event is scheduled (or
     *               <code>null</code> if not applicable)
     * @param at     the TimeInstant at which the event is scheduled
     * @param remark optional further comments (or <code>null</code> if not applicable)
     */
    protected void generateTraceForScheduling(EventAbstract Event, Entity who1, Entity who2, Schedulable after,
                                              Schedulable before, TimeInstant at, String remark) {

        if (currentlySendTraceNotes()) {

            StringBuilder trace = new StringBuilder("schedules '" + Event.getName() + "'");
            if (who1 != null) {
                String who1alias = (who1 == currentEntity() ? "itself" : "'" + who1.getName() + "'");
                trace.append(" with " + who1alias);
                if (who2 != null) {
                    String who2alias = (who2 == currentEntity() ? "itself" : "'" + who2.getName() + "'");
                    trace.append(" and " + who2alias);
                }
            }

            if (after != null) {
                String afterAlias = (after == currentEntity() ? "itself" : "'" + after.getName() + "'");
                trace.append(" after " + afterAlias);
            } else if (before != null) {
                String beforeAlias = (before == currentEntity() ? "itself" : "'" + before.getName() + "'");
                trace.append(" before " + beforeAlias);
            }

            if (at == this.presentTime()) {
                trace.append(" now.");
            } else {
                trace.append(" at " + at.toString() + ".");
            }

            if (remark != null && remark.length() > 0) {
                trace.append(". " + remark);
            }

            this.sendTraceNote(trace.toString());
        }
    }

    /**
     * Informs this <code>Entity</code> to be queued in a given
     * <code>QueueBased</code>.
     *
     * @param q QueueBased : The <code>QueueBased</code> where this entity is now queued.
     */
    void addQueueBased(QueueBased q) {
        _myQueues.add(q);
    }

    /**
     * Removes an event-note from the internal list
     *
     * * @param note EventNote : The <code>EventNote to be removed</code>
     */
    @Override
    void removeEventNote(EventNote note) {
        _schedule.remove(note); // only removes Event in local list

    }

    /**
     * Informs this <code>Entity</code> to be no longer queued in a given
     * <code>QueueBased</code>.
     *
     * @param q Queue<?> : The <code>QueueBased</code> where this entity is no longer queued.
     */
    void removeQueueBased(QueueBased q) {
        _myQueues.remove(q);
    }
}
