package desmoj.core.simulator;

import desmoj.core.dist.NumericalDist;

/**
 * Provides the class for user defined events to change the model's state at a distinct point in simulation time.
 * <p>
 * For events specifically changing the state of up to three entities, refer to
 * <code>Event</code>, <code>EventOf2Entities</code> and
 * <code>EventOf3Entities</code>. Events not associated to a specific entity are
 * called external events as they are are typically used for external influences, e.g. arrivals from outside or
 * incidents.
 *
 *
 * Derive from this class to design special external events for a model. To use external events, always create a new
 * object of this class.
 *
 * @author Tim Lechler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @see Event
 * @see EventOf2Entities
 * @see EventOf3Entities
 * @see TimeInstant
 * @see TimeSpan
 */
public abstract class ExternalEvent extends EventAbstract {

    /**
     * Creates an external event for the given model with the given name and the specified tracemode.
     *
     * @param owner       Model : The external event's model
     * @param name        java.lang.String : The external event's name
     * @param showInTrace boolean : Flag for showing this external event in tracemessages
     */
    public ExternalEvent(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace);
        this.numberOfEntities = 0;

    }

    /**
     * Implement this method to express the semantics of this external event. External events are supposed to act on the
     * model or experiment in general. They are not related to a special Entity (unlike method
     * <code>eventRoutine(Entity who)</code> of class <code>Event</code>). Override
     * this method to implement this Externalevent's behaviour.
     */
    public abstract void eventRoutine();

    /**
     * Schedules this external event to make the desired changes to the experiment or model now. No preemption, i.e. a
     * process calling this method will continue until passivated or hold.
     *
     * @see SimClock
     */
    public void schedule() {

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, null, presentTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(null, this, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Schedules this external event to make the desired changes to the experiment or model. The time instant for which
     * the event is scheduled is determined by a sample from the distribution provided to the method. The sample is
     * interpreted as offset from the the present time in the reference time unit.
     *
     * @param dist NumericalDist<?> : Numerical distribution to sample the offset to the current simulation time from
     * @see SimClock
     */
    public void schedule(NumericalDist<?> dist) {

        if ((dist == null)) {
            sendWarning("Can't schedule external event!",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity who, NumericalDist<?> dist)",
                        "The NumericalDist given as parameter is a null " + "reference.",
                        "Be sure to have a valid NumericalDist reference before calling " + "this method.");
            return; // no proper parameter
        }

        // determine time span
        TimeSpan dt = dist.sampleTimeSpan();

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, null, TimeOperations.add(presentTime(), dt),
                                        " Sampled from " + dist.getQuotedName() + ".");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(null, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Schedules this external event to make the desired changes to the experiment or model at the specified point in
     * simulation time. No preemption, i.e. a process calling this method will continue until passivated or hold.
     *
     * @param when TimeInstant : The point in simulation time this external event is scheduled to happen.
     * @see SimClock
     */
    public void schedule(TimeInstant when) {
        if ((when == null)) {
            sendWarning("Can't schedule external event!",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity who, TimeInstant when)",
                        "The point of simulation time given as parameter is a null " + "reference.",
                        "Be sure to have a valid TimeInstant reference before calling " + "this method.");
            return; // no proper parameter
        }

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, null, when, null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(null, this, when);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Schedules this external event to make the desired changes to the experiment or model at the current point of time
     * plus the given span of time. No preemption, i.e. a process calling this method will continue until passivated or
     * hold.
     *
     * @param dt TimeSpan : The offset to the current simulation time at which this external event is to be scheduled
     * @see SimClock
     */
    public void schedule(TimeSpan dt) {
        if ((dt == null)) {
            sendWarning("Can't schedule external event!",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity who, TimeSpan dt)",
                        "The simulation time given as parameter is a null " + "reference.",
                        "Be sure to have a valid TimeSpan reference before calling " + "this method.");
            return; // no proper parameter
        }

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, null, TimeOperations.add(presentTime(), dt), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(null, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Schedules this external event to act on the experiment or model state directly after the given Schedulable is
     * already set to be activated. Note that this external event's point of simulation time will be set to be the same
     * as the Schedulable's time. Thus this external event will occur directly after the given Schedulable but the
     * simulation clock will not change. Make sure that the Schedulable given as parameter is actually scheduled.
     *
     * @param after Schedulable : The Schedulable this external event should be scheduled after
     */
    public void scheduleAfter(Schedulable after) {

        if ((after == null)) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleAfter(Schedulable after, " + "Entity who)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this "
                        + "external event to be scheduled with.");
            return; // no proper parameter
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleAfter(Schedulable after)",
                        "The Schedulable '" + after.getName() + "' given as a positioning "
                        + "reference has to be already scheduled but is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        // generate trace
        this.generateTraceForScheduling(null, null, null, after, null,
                                        after.getEventNotes().get(after.getEventNotes().size() - 1).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleAfter(after, null, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleAfter " + after.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                     .getScheduler()
                                                                                                     .toString());
        }

    }

    /**
     * Schedules this external event to act on the experiment or model state directly before the given Schedulable is
     * already set to be activated. Note that this external event's point of simulation time will be set to be the same
     * as the Schedulable's time. Thus this external event will occur directly before the given Schedulable but the
     * simulation clock will not change. Make sure that the Schedulable given as parameter is actually scheduled.
     *
     * @param before Schedulable : The Schedulable this external event should be scheduled before
     */
    public void scheduleBefore(Schedulable before) {

        if ((before == null)) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleBefore(Schedulable before, " + "Entity who)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this "
                        + "external event to be scheduled with.");
            return; // no proper parameter
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleBefore(Schedulable before)",
                        "The Schedulable '" + before.getName() + "' given as a "
                        + "positioning reference has to be already " + "scheduled but is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, before, before.getEventNotes().get(0).getTime(), null);

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleBefore(before, null, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                       .getScheduler()
                                                                                                       .toString());
        }

    }

    /**
     * Schedules this event to be executed immediately, preempting the process lifecycle executed at the moment.
     *
     * @throws SuspendExecution
     */
    public void schedulePreempt() {

        if (getModel().getExperiment().getScheduler().getCurrentSimProcess() == null) {
            sendWarning("Can't preempt current SimProcess! " + "Command ignored.",
                        "ExternalEvent : " + getName() + " Method: schedulePreempt()", "No current process.",
                        "Call this method during process execution only.");
            return; // preemption of currentprocess only
        }

        // generate trace
        this.generateTraceForScheduling(null, null, null, null, null, presentTime(), "preempted");

        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(null, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Creates and returns a copy of this event. Note that subclasses have to implement the interface
     * <code>java.lang.Cloneable</code> to actually use this method as otherwise, a
     * <code>CloneNotSupportedException</code> will be thrown.
     *
     * @return ExternalEvent : A copy of this event.
     */
    @Override
    protected ExternalEvent clone() throws CloneNotSupportedException {
        return (ExternalEvent) super.clone();
    }
}
