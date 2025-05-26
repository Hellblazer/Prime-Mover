package desmoj.core.simulator;

/**
 * The external event to stop a running experiment.
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
 */
public class ExternalEventStop extends ExternalEvent {
    /**
     * Constructs an external event to abort the experiment.
     *
     * @param owner       desmoj.Model : The model this external event is associated to
     * @param name        java.lang.String : The external event's name
     * @param showInTrace boolean : Flag indicating if the external event is shown in the report
     */
    public ExternalEventStop(Model owner, String name, boolean showInTrace) {

        super(owner, name, showInTrace);

    }

    /**
     * The eventRoutine to abort an experiment.
     */
    @Override
    public void eventRoutine() {

        getModel().getExperiment().setStatus(Experiment.STOPPED);

    }

    /**
     * Do not use this method to implement the external event's eventRoutine! This method can not be hidden due to the
     * inheritance relationship to the class
     * <code>Event</code>. Since external events are designed to act on the model or
     * experiment in general and are not associated to an individual Entity, you should use the parameterless method
     * <code>void eventRoutine()</code> instead. Calling this method will result in a warning message and the
     * parameterless method will be called. The given Entity will not be changed. Do not override this method in your
     * special external events!
     *
     * @see ExternalEvent#eventRoutine()
     */
    public void eventRoutine(Entity who) {

        // send warning only if call was intended to be non-ExternalEvent
        // if null was given, it was intended to be an ExternalEvent
        if (who != null) {
            sendWarning("Can't accept Entity as parameter",
                        "ExternalEvent : " + getName() + " Method: void eventRoutine(Entity who)",
                        "External events do not act on entities.",
                        "If you want an event to act on the given Entity use the " + "class Event and override the"
                        + "eventRoutine(Entity who) method in that class.");
        }

        eventRoutine();

    }

    /**
     * Schedules this external event to make the desired changes to the experiment or model at the specified point in
     * simulation time.
     *
     * @param when TimeInstant : The point in simulation time this external event is scheduled to happen.
     * @see SimClock
     */
    @Override
    public void schedule(TimeInstant when) {
        if ((when == null)) {
            sendWarning("Can't schedule external event!",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity who, TimeInstant when)",
                        "The point of simulation time given as parameter is a null " + "reference.",
                        "Be sure to have a valid TimeInstant reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity wo, TimeInstant when)",
                        "The external event to be scheduled is already scheduled.",
                        "Use external events only once, do not reuse them " + "multiple times.");
            return; // was already scheduled
        }

        if (currentlySendTraceNotes()) {
            sendTraceNote("ExternalEvent '" + getName() + "' scheduled at " + when.toString());
        }

        getModel().getExperiment().getScheduler().scheduleNoPreempt(null, this, when);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>" + getModel().getExperiment().getScheduler().toString());
        }
    }

    /**
     * Schedules this external event to make the desired changes to the experiment or model at the current point of time
     * plus the given span of time
     *
     * @param dt TimeSpan : The offset to the current simulation time at which this external event is to be scheduled
     * @see SimClock
     */
    @Override
    public void schedule(TimeSpan dt) {
        if ((dt == null)) {
            sendWarning("Can't schedule external event!",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity who, TimeSpan dt)",
                        "The simulation time given as parameter is a null " + "reference.",
                        "Be sure to have a valid TimeSpan reference before calling " + "this method.");
            return; // no proper parameter
        }

        if (isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: schedule(Entity wo, TimeSpan dt)",
                        "The external event to be scheduled is already scheduled.",
                        "Use external events only once, do not reuse them " + "multiple times.");
            return; // was already scheduled
        }

        if (currentlySendTraceNotes()) {
            sendTraceNote(
            "ExternalEvent '" + getName() + "' scheduled at " + TimeOperations.add(presentTime(), dt).toString());
            // getModel().getExperiment().getTimeFloats()));
        }

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
    @Override
    public void scheduleAfter(Schedulable after) {

        if ((after == null)) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleAfter(Schedulable after, " + "Entity who)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this "
                        + "external event to be scheduled with.");
            return; // no proper parameter
        }

        if (isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleAfter(Schedulable after)",
                        "The external event to be scheduled is already scheduled.",
                        "Use method external events only once, do not use them " + "multiple times.");
            return; // was already scheduled
        }

        if (!after.isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleAfter(Schedulable after)",
                        "The Schedulable '" + after.getName() + "' given as a positioning "
                        + "reference has to be already scheduled but is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        if (currentlySendTraceNotes()) {
            sendTraceNote(
            "external event '" + getName() + "' scheduled after '" + after.getName() + "' at " + after.getEventNotes()
                                                                                                      .get(
                                                                                                      after.getEventNotes()
                                                                                                           .size() - 1)
                                                                                                      .getTime()
                                                                                                      .toString());
        }

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
    @Override
    public void scheduleBefore(Schedulable before) {

        if ((before == null)) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleBefore(Schedulable before, " + "Entity who)",
                        "The Schedulable given as parameter is a null reference.",
                        "Be sure to have a valid Schedulable reference for this "
                        + "external event to be scheduled with.");
            return; // no proper parameter
        }

        if (isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleBefore(Schedulable before)",
                        "The external event to be scheduled is already scheduled.",
                        "Use method external events only once, do not use them " + "multiple times.");
            return; // was already scheduled
        }

        if (!before.isScheduled()) {
            sendWarning("Can't schedule external event! Command ignored.",
                        "ExternalEvent : " + getName() + " Method: scheduleBefore(Schedulable before)",
                        "The Schedulable '" + before.getName() + "' given as a "
                        + "positioning reference has to be already " + "scheduled but is not.",
                        "Use method isScheduled() of any Schedulable to find out " + "if it is already scheduled.");
            return; // was not scheduled
        }

        if (currentlySendTraceNotes()) {
            sendTraceNote("external event '" + getName() + "' scheduled before '" + before.getName() + "' at "
                          + before.getEventNotes().get(0).getTime().toString());
        }

        getModel().getExperiment().getScheduler().scheduleBefore(before, null, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("scheduleBefore " + before.getQuotedName() + " on EventList<br>" + getModel().getExperiment()
                                                                                                       .getScheduler()
                                                                                                       .toString());
        }

    }
}
