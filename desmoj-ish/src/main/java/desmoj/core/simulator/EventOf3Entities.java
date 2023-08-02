package desmoj.core.simulator;

import desmoj.core.dist.NumericalDist;

/**
 * Provides the class for user defined events to change <b>three</b> entities'
 * internal states. The state of a discrete model is changed by events that
 * occur at distinct points of simulation time.
 * <p>
 * For events changing the state of <b>one</b> or <b>two</b> entities, refer to
 * <code>Event</code> and <code>EventOf2Entities</code>. Events not associated
 * to a specific entity are based on <code>ExternalEvent</code>.
 * <p>
 * For type safety it is recommended to generically assign the entity types an
 * <code>EventOf3Entities</code> operates on by using the generic type
 * <code>EventOf3Entities&lt;E,F,G&gt;</code> where <code>E</code>,
 * <code>F</code> and <code>G</code> are derived from <code>Entity</code>.
 * <p>
 * All event object should be used only once. Implement the changes of state for
 * the specific entities associated with this event by overriding the abstract
 * method <code>eventRoutine(E who1, F who2, G who3)</code>.
 * 
 * @see Entity
 * @see ExternalEvent
 * @see Event
 * @see EventOf2Entities
 * @see TimeInstant
 * @see TimeSpan
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * @author modified by Justin Neumann
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
public abstract class EventOf3Entities<E extends Entity, F extends Entity, G extends Entity> extends EventAbstract {

    /**
     * Creates a new Event of the given model, with the given name and trace option.
     * 
     * @param name        java.lang.String : The name of this event
     * @param owner       Model : The model this event is associated to
     * @param showInTrace boolean : Flag for showing Event in trace-files. Set it to
     *                    <code>true</code> if Event should show up in trace. Set it
     *                    to <code>false</code> if Event should not be shown in
     *                    trace.
     */
    public EventOf3Entities(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace);
        this.numberOfEntities = 3;

    }

    /**
     * Implement this abstract method to express the changes of state this event
     * does to three entities (as <code>SimProcess</code> is a subclass of
     * <code>Entity</code>, processes can be passed as well).
     * <p>
     * For type safety, it is recommended to derive your events from the generic
     * type <code>EventOf3Entities&lt;EntityType1,EntityType2,EntityType3&gt;</code>
     * where <code>EntityType1</code>, <code>EntityType2</code> and
     * <code>EntityType3</code> (which are derived from class <code>Entity</code>)
     * represent the entity types your event is supposed to operate on.
     * <p>
     * Should you decide to derive your event from the raw type
     * <code>EventOf3Entities</code> (which is not recommended), please take extra
     * care in checking the given Entity parameters to your special eventRoutine
     * since any subtype of Entity will be accepted! If your model uses several
     * different entity types, chances are that while developing the model, wrong
     * entity types might be passed.
     * 
     * @param who1 E : The first entity associated to this event.
     * @param who2 F : The second entity associated to this event.
     * @param who3 G : The second entity associated to this event.
     */
    public abstract void eventRoutine(E who1, F who2, G who3);

    /**
     * Schedules this event to act on the given entities now. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     * 
     * @param who1 E : The first entity this event happens to
     * @param who2 F : The second entity this event happens to
     * @param who3 G : The third entity this event happens to
     * @see SimClock
     */
    public void schedule(E who1, F who2, G who3) {

        if ((who1 == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The first entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The second entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The third entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, null, presentTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who1, who2, who3, this, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to act on the given entities at the specified point in
     * simulation time determined by a sample from the distribution provided to the
     * method. The sample is interpreted as offset from the the present time in the
     * reference time unit.
     * 
     * @param who1 E : The first entity this event happens to
     * @param who2 F : The second entity this event happens to
     * @param who3 G : The third entity this event happens to
     * @param dist NumericalDist<?> : Numerical distribution to sample the offset to
     *             the current simulation time from
     * 
     * @see SimClock
     */
    public void schedule(E who1, F who2, G who3, NumericalDist<?> dist) {

        if ((dist == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The NumericalDist given as parameter is a null reference.",
                        "Be sure to have a valid NumericalDist reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((who1 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The first entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The second entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The third entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, NumericalDist<?> dist)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // determine time span
        TimeSpan dt = dist.sampleTimeSpan();

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, null, TimeOperations.add(presentTime(), dt),
                                        " Sampled from " + dist.getQuotedName() + ".");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who1, who2, who3, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to act on the given entities at a certain point in
     * simulation time. No preemption, i.e. a process calling this method will
     * continue until passivated or hold.
     * 
     * @param who1    E : The first entity to be manipulated by this event
     * @param who2    F : The second entity to be manipulated by this event
     * @param who3    G : The third entity to be manipulated by this event
     * @param instant TimeInstant : The point in simulation time this event is
     *                scheduled to happen.
     */
    public void schedule(E who1, F who2, G who3, TimeInstant instant) {

        if ((instant == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The TimeInstant given as parameter is a null reference.",
                        "Be sure to have a valid TimeInstant reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((who1 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The first entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The second entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The third entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: eventRoutine(E who1, F who2, G who3)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, null, instant, null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who1, who2, who3, this, instant);
        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to act on the given entities at the specified point in
     * simulation time. The point of time is given as an offset to the current
     * simulation time as displayed by the simulation clock. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     * 
     * @param who1 E : The first entity this event happens to
     * @param who2 F : The second entity this event happens to
     * @param who3 G : The third entity this event happens to
     * @param dt   TimeSpan : The offset to the current simulation time this Event
     *             is to happen
     * @see SimClock
     */
    public void schedule(E who1, F who2, G who3, TimeSpan dt) {
        if ((dt == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The TimeSpan given as parameter is a null reference.",
                        "Be sure to have a valid TimeSpan reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((who1 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The first entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The second entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The third entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedule(E who1, F who2, G who3, TimeSpan dt)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, null, TimeOperations.add(presentTime(), dt), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who1, who2, who3, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to act on the given Entities directly after the given
     * Schedulable is already set to be activated. Note that this event's point of
     * simulation time will be set to be the same as the Schedulable's time. Thus
     * this event will occur directly after the given Schedulable but the simulation
     * clock will not change. Make sure that the Schedulable given as parameter is
     * actually scheduled.
     * 
     * @param after Schedulable : The Schedulable this entity should be scheduled
     *              after
     * @param who1  E : The first entity to be manipulated by this event
     * @param who2  F : The second entity to be manipulated by this event
     * @param who3  G : The third entity to be manipulated by this event
     */
    public void scheduleAfter(Schedulable after, E who1, F who2, G who3) {

        if (who1 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who1' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (who2 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (who3 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (after == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Event to be scheduled with.");
            return; // no proper parameter
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The Schedulable '" + after.getName() + "' given as a positioning "
                        + "reference has to be already scheduled but is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The first Entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: scheduleAfter(Schedulable after, E who1, F who2, G who3)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, after, null,
                                        after.getEventNotes().get(after.getEventNotes().size() - 1).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleAfter(after, who1, who2, who3, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleAfter " + after.getQuotedName() + " on EventList<br>"
            + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to act on the given Entities directly before the given
     * Schedulable is already set to be activated. Note that this event's point of
     * simulation time will be set to be the same as the Schedulable's time. Thus
     * this event will occur directly before the given Schedulable but the
     * simulation clock will not change. Make sure that the Schedulable given as
     * parameter is actually scheduled.
     * 
     * @param before Schedulable : The Schedulable this entity should be scheduled
     *               before
     * @param who1   E : The first entity to be manipulated by this event
     * @param who2   F : The second entity to be manipulated by this event
     * @param who3   G : The third entity to be manipulated by this event
     */
    public void scheduleBefore(Schedulable before, E who1, F who2, G who3) {

        if (who1 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who1' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (who2 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who2' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (who3 == null) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable after, E who1, F who2, G who3)",
                        "The Entity 'who3' given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference before calling " + "this method.");
            return; // no proper parameter
        }

        if ((before == null)) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable before, E who1, F who2, G who3)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this " + "Event to be scheduled with.");
            return; // no proper parameter
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule Event! Command ignored.",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable after, "
                        + " Method: scheduleBefore(Schedulable before, E who1, F who2, G who3)",
                        "The Schedulable '" + before.getName() + "' given as a "
                        + "positioning reference has to be already scheduled but " + "is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable before, E who1, F who2, G who3)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable before, E who1, F who2, G who3)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Event : " + getName() + " Method: scheduleBefore(Schedulable before, E who1, F who2, G who3)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, before, before.getEventNotes().get(0).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleBefore(before, who1, who2, who3, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>"
            + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Schedules this event to be executed immediately, preempting the process
     * lifecycle executed at the moment.
     * 
     * @param who1 E : The first entity this event happens to
     * @param who2 F : The second entity this event happens to
     * @param who3 G : The third entity this event happens to
     * @see SimClock
     */
    public void schedulePreempt(E who1, F who2, G who3) {

        if (getModel().getExperiment().getScheduler().getCurrentSimProcess() == null) {
            sendWarning("Can't preempt current SimProcess! " + "Command ignored.",
                        "EventOf3Entities : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "No current process.", "Call this method during process execution only.");
            return; // preemption of currentprocess only
        }

        if ((who1 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The first entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who2 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The second entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if ((who3 == null)) {
            sendWarning("Can't schedule Event!",
                        "Event : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The third entity given as parameter is a null reference.",
                        "Be sure to have a valid Entity reference for this event to " + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who1)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The first entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who2)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The second entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        if (!isModelCompatible(who3)) {
            sendWarning("Can't schedule Event! Command ignored",
                        "Entity : " + getName() + " Method: schedulePreempt(E who1, F who2, G who3)",
                        "The third entity to be scheduled with this event is not " + "modelcompatible.",
                        "Make sure to use compatible model components only.");
            return; // is not compatible
        }

        // generate trace
        this.generateTraceForScheduling(who1, who2, who3, null, null, presentTime(), "preempted");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(who1, who2, who3, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }

    }

    /**
     * Creates and returns a copy of this event. Note that subclasses have to
     * implement the interface <code>java.lang.Cloneable</code> to actually use this
     * method as otherwise, a <code>CloneNotSupportedException</code> will be
     * thrown.
     * 
     * @return EventOf3Entities<E,F,G> : A copy of this event.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected EventOf3Entities<E, F, G> clone() throws CloneNotSupportedException {
        return (EventOf3Entities<E, F, G>) super.clone();
    }
}
