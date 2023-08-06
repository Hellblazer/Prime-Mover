package desmoj.core.simulator;


import desmoj.core.dist.NumericalDist;

/**
 * Provides the class for user defined events to change <b>a single</b> entity's
 * internal state. The state of a discrete model is changed by events that occur
 * at distinct points of simulation time.<p>
 * For events changing the state of <b>two</b> or <b>three</b> entities,
 * refer to <code>EventOf2Entities</code> and <code>EventOf3Entities</code>.
 * Events not associated to a specific entity are based on <code>ExternalEvent</code>.
 * <p>
 * For type safety it is recommended to generically assign the entity type an
 * Event operates on by using the generic type
 * <code>Event&lt;E&gt;</code> where
 * <code>E</code> is derived from <code>Entity</code>.
 * <p>
 * All event object should be used only once. Implement the changes of state for the 
 * specific entity associated with this event by overriding the abstract method 
 * <code>eventRoutine(E who)</code>.
 * 
 * @see Entity
 * @see ExternalEvent
 * @see EventOf2Entities
 * @see EventOf3Entities
 * @see TimeInstant
 * @see TimeSpan
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
public abstract class Event<E extends Entity> extends EventAbstract {


	/**
	 * Creates a new event of the given model, with the given name and trace
	 * option.
	 * 
	 * @param name
	 *            java.lang.String : The name of this event
	 * @param owner
	 *            Model : The model this event is associated to
	 * @param showInTrace
	 *            boolean : Flag for showing event in trace-files. Set it to
	 *            <code>true</code> if event should show up in trace. Set it to
	 *            <code>false</code> if event should not be shown in trace.
	 */
	public Event(Model owner, String name, boolean showInTrace) {

		super(owner, name, showInTrace);
		this.numberOfEntities = 1;

	}

	/**
     * Creates and returns a copy of this event.
     * Note that subclasses have to implement the interface 
     * <code>java.lang.Cloneable</code> to actually use this method as 
     * otherwise, a <code>CloneNotSupportedException</code> will be thrown.
     * 
     * @return Event<E> : A copy of this event.
     */  
	@SuppressWarnings("unchecked")
    public Event<E> clone() throws CloneNotSupportedException {
	    return (Event<E>) super.clone();
	}

    /**
	 * Implement this abstract method to express the changes of state this event
	 * does to a single entity. This event is related to the entity it has been
	 * scheduled with. That Entity is given note that since class
	 * <code>SimProcess</code> inherits from class <code>Entity</code>, an event
	 * can also be given a process to operate on. In this case, the process is
	 * scheduled and manipulated by this event just like an entity.
	 * 
	 * Implement this abstract method to express the changes of state this event
     * does to an entity (as <code>SimProcess</code> is a subclass of 
     * <code>Entity</code>, a process can be passed as well).
	 * <p>
	 * For type safety, it is recommended to derive your events from the generic
	 * type <code>Event&lt;EntityOperatingOn&gt;</code> where
	 * <code>EntityOperatingOn</code> (which is derived from class
	 * <code>Entity</code>) represents the entity type your event is supposed to
	 * operate on.
	 * <p>
	 * Should you decide to derive your event from the raw type
	 * <code>Event</code> (which is not recommended), please take extra care in
	 * checking the given Entity parameter to your special eventRoutine since
	 * any subtype of Entity will be accepted! If your model uses several
	 * different entity types, chances are that while developing the model,
	 * wrong entity types might be passed. 
	 * 
	 * @param who
	 *            Entity : The Entity associated to this event.
	 * @throws SuspendExecution Marker exception for Quasar.
	 */
	public abstract void eventRoutine(E who);

	/**
     * Schedules this event to act on the given Entity now. Preemption 
     * is not allowed.
     * 
     * @param who
     *            E : The Entity this event happens to
     * @see SimClock
     */
    public void schedule(E who) {

        if ((who == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedule(Entity who)",
                    "The Entity given as parameter is a null reference.",
                    "Be sure to have a valid Entity reference for this event to "
                            + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who)) {
            sendWarning("Can't schedule Event! Command ignored", "Entity : "
                    + getName() + " Method: schedule(Entity who)",
                    "The Entity to be scheduled with this event is not "
                            + "modelcompatible.",
                    "Make sure to use compatible model components only.");
            return; // is not compatible
        }
        
        // generate trace
        this.generateTraceForScheduling(who, null, null, null, null, presentTime(), null);
        
        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who, this, presentTime());

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

    }
    
    /**
     * Schedules this event to act on the given Entity at a point in
     * simulation time determined by a sample from the distribution provided
     * to the method. The sample is interpreted as offset from the the present 
     * time in the reference time unit.
     * 
     * @param who
     *            E : The Entity this event happens to
     * @param dist
     *            NumericalDist<?> : Numerical distribution to sample the 
     *            offset to the current simulation time from
     * @see SimClock
     */
    public void schedule(E who, NumericalDist<?> dist) {
        
        if ((dist == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedule(Entity who, NumericalDist<?> dist)",
                    "The NumericalDist given as parameter is a null reference.",
                    "Be sure to have a valid NumericalDist reference before calling "
                            + "this method.");
            return; // no proper parameter
        }

        if ((who == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedule(Entity who, NumericalDist<?> dist)",
                    "The Entity given as parameter is a null reference.",
                    "Be sure to have a valid Entity reference for this event to "
                            + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who)) {
            sendWarning("Can't schedule Event! Command ignored", "Entity : "
                    + getName() + " Method: schedule(Entity who, NumericalDist<?> dist)",
                    "The Entity to be scheduled with this event is not "
                            + "modelcompatible.",
                    "Make sure to use compatible model components only.");
            return; // is not compatible
        }
        
        // determine time span
        TimeSpan dt = dist.sampleTimeSpan();
        
        // generate trace
        this.generateTraceForScheduling(who, null, null, null, null, TimeOperations.add(presentTime(), dt), "sampled from " + dist.getQuotedName());
        
        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

    }
    
    /**
     * Schedules this event to act on the given entity at a certain point in 
     * simulation time. No preemption, i.e. a process calling this method
     * will continue until passivated or hold. 
     * 
     * @param who
     *            E : The first entity to be manipulated by this event
     * @param instant
     *            TimeInstant : The point in simulation time this event is
     *            scheduled to happen.
     */
	public void schedule(E who, TimeInstant instant) {

		if ((instant == null)) {
			sendWarning("Can't schedule Event!", "Event : " + getName()
					+ " Method: schedule(Entity who, TimeInstant instant)",
					"The TimeInstant given as parameter is a null reference.",
					"Be sure to have a valid TimeInstant reference before calling "
							+ "this method.");
			return; // no proper parameter
		}

		if ((who == null)) {
			sendWarning("Can't schedule Event!", "Event : " + getName()
					+ " Method: schedule(Entity who, TimeInstant instant)",
					"The Entity given as parameter is a null reference.",
					"Be sure to have a valid Entity reference for this event to "
							+ "be scheduled with.");
			return; // no proper parameter
		}

		if (!isModelCompatible(who)) {
			sendWarning("Can't schedule Event! Command ignored", "Entity : "
					+ getName()
					+ " Method: schedule(Entity who, TimeInstant instant)",
					"The Entity to be scheduled with this event is not "
							+ "modelcompatible.",
					"Make sure to use compatible model components only.");
			return; // is not compatible
		}
		
		// generate trace
		this.generateTraceForScheduling(who, null, null, null, null, instant, null);
		
		// schedule Event
		getModel().getExperiment().getScheduler().scheduleNoPreempt(who, this, instant);

		if (currentlySendDebugNotes()) {
			sendDebugNote("schedules on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

	}
    
    /**
     * Schedules this event to act on the given Entity at the specified point in
     * simulation time. The point of time is given as an offset to the current
     * simulation time as displayed by the simulation clock. Preemption 
     * is not allowed.
     * 
     * @param who
     *            E : The Entity this event happens to
     * @param dt
     *            TimeSpan : The offset to the current simulation time this
     *            Event is to happen
     * @see SimClock
     */
    public void schedule(E who, TimeSpan dt) {
        if ((dt == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedule(Entity who, TimeSpan dt)",
                    "The TimeSpan given as parameter is a null reference.",
                    "Be sure to have a valid TimeSpan reference before calling "
                            + "this method.");
            return; // no proper parameter
        }

        if ((who == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedule(Entity who, TimeSpan dt)",
                    "The Entity given as parameter is a null reference.",
                    "Be sure to have a valid Entity reference for this event to "
                            + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who)) {
            sendWarning("Can't schedule Event! Command ignored", "Entity : "
                    + getName() + " Method: schedule(Entity who, TimeSpan dt)",
                    "The Entity to be scheduled with this event is not "
                            + "modelcompatible.",
                    "Make sure to use compatible model components only.");
            return; // is not compatible
        }
        
        // generate trace
        this.generateTraceForScheduling(who, null, null, null, null, TimeOperations.add(presentTime(), dt), null);
        
        // schedule Event
        getModel().getExperiment().getScheduler().scheduleNoPreempt(who, this, dt);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

    }

	/**
	 * Schedules this event to act on the given Entity directly after the given
	 * Schedulable is already set to be activated. Note that this event's point
	 * of simulation time will be set to be the same as the Schedulable's time.
	 * Thus this event will occur directly after the given Schedulable but the
	 * simulation clock will not change. Make sure that the Schedulable given as
	 * parameter is actually scheduled.
	 * 
	 * @param after
	 *            Schedulable : The Schedulable this entity should be scheduled
	 *            after
	 * @param who
	 *            E : The Entity to be manipulated by this event
	 */
	public void scheduleAfter(Schedulable after, E who) {

		if (who == null) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName() + " Method: scheduleAfter(Schedulable after, "
					+ "Entity who)",
					"The Entity 'who' given as parameter is a null reference.",
					"Be sure to have a valid Entity reference before calling "
							+ "this method.");
			return; // no proper parameter
		}

		if (after == null) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName() + " Method: scheduleAfter(Schedulable after, "
					+ "Entity who)",
					"The Schedulable given as parameter is a null reference.",
					"Be sure to have a valid Schedulable reference for this "
							+ "Event to be scheduled with.");
			return; // no proper parameter
		}

		if (who.isScheduled()) {
			sendWarning("Can't schedule Event! Command ignored.", "Entity : "
					+ getName() + " Method: scheduleAfter(Schedulable after, "
					+ "Entity who)", "The Entity '" + who.getName()
					+ "'to be scheduled with this "
					+ "Event is already scheduled.",
					"Use method reSchedule(TimeSpan dt) to shift the entity "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (!after.isScheduled()) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName() + " Method: scheduleAfter(Schedulable after, "
					+ "Entity who)", "The Schedulable '" + after.getName()
					+ "' given as a positioning "
					+ "reference has to be already scheduled but is not.",
					"Use method isScheduled() of any Schedulable to find out "
							+ "if it is already scheduled.");
			return; // was not scheduled
		}

		if (!isModelCompatible(who)) {
			sendWarning("Can't schedule Event! Command ignored", "Entity : "
					+ getName() + " Method: scheduleAfter(Schedulable after, "
					+ "Entity who)",
					"The Entity to be scheduled with this event is not "
							+ "modelcompatible.",
					"Make sure to use compatible model components only.");
			return; // is not compatible
		}
		
        // generate trace
        this.generateTraceForScheduling(who, null, null, after, null, after.getEventNotes().get(after.getEventNotes().size()-1).getTime(), null);
        
        // schedule Event
		getModel().getExperiment().getScheduler().scheduleAfter(after, who,
				this);

		if (currentlySendDebugNotes()) {
			sendDebugNote("scheduleAfter " + after.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

	}

	/**
	 * Schedules this event to act on the given Entity directly before the given
	 * Schedulable is already set to be activated. Note that this event's point
	 * of simulation time will be set to be the same as the Schedulable's time.
	 * Thus this event will occur directly before the given Schedulable but the
	 * simulation clock will not change. Make sure that the Schedulable given as
	 * parameter is actually scheduled.
	 * 
	 * @param before
	 *            Schedulable : The Schedulable this entity should be scheduled
	 *            before
	 * @param who
	 *            E : The Entity to be manipulated by this event
	 */
	public void scheduleBefore(Schedulable before, E who) {

		if ((who == null)) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName()
					+ " Method: scheduleBefore(Schedulable before, "
					+ "Entity who)",
					"The Entity given as parameter is a null reference.",
					"Be sure to have a valid Entity reference before calling "
							+ "this method.");
			return; // no proper parameter
		}

		if ((before == null)) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName()
					+ " Method: scheduleBefore(Schedulable before, "
					+ "Entity who)",
					"The Schedulable given as parameter is a null reference.",
					"Be sure to have a valid Schedulable reference for this "
							+ "Event to be scheduled with.");
			return; // no proper parameter
		}

		if (who.isScheduled()) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName()
					+ " Method: scheduleBefore(Schedulable before, "
					+ "Entity who)", "The Entity '" + who.getName()
					+ "'to be scheduled with this "
					+ "Event is already scheduled.",
					"Use method reSchedule(TimeSpan dt) to shift the entity "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (!before.isScheduled()) {
			sendWarning("Can't schedule Event! Command ignored.", "Event : "
					+ getName() + " Method: scheduleBefore(Schedulable after, "
					+ "Entity who)", "The Schedulable '" + before.getName()
					+ "' given as a "
					+ "positioning reference has to be already scheduled but "
					+ "is not.",
					"Use method isScheduled() of any Schedulable to find out "
							+ "if it is already scheduled.");
			return; // was not scheduled
		}

		if (!isModelCompatible(who)) {
			sendWarning("Can't schedule Event! Command ignored", "Event : "
					+ getName()
					+ " Method: scheduleBeforer(Schedulable before, "
					+ "Entity who)",
					"The Entity to be scheduled with this event is not "
							+ "modelcompatible.",
					"Make sure to use compatible model components only.");
			return; // is not compatible
		}

        // generate trace
        this.generateTraceForScheduling(who, null, null, null, before, before.getEventNotes().get(0).getTime(), null);
        
        // schedule Event
		getModel().getExperiment().getScheduler().scheduleBefore(before, who,
				this);

		if (currentlySendDebugNotes()) {
			sendDebugNote("scheduleBefore " + before.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

	}
	
    /**
     * Schedules this event to be executed immediately, preempting the
     * process lifecycle executed at the moment.
     * 
     * @param who
     *            E : The Entity this event happens to
     * @throws SuspendExecution 
     */
    public void schedulePreempt(E who) {
        
        if (getModel().getExperiment().getScheduler().getCurrentSimProcess() == null) {
            sendWarning("Can't preempt current SimProcess! "
                    + "Command ignored.", "Event : " + getName()
                    + " Method: schedulePreempt(E who)",
                    "No current process.",
                    "Call this method during process execution only.");
            return; // preemption of currentprocess only
        }

        if ((who == null)) {
            sendWarning("Can't schedule Event!", "Event : " + getName()
                    + " Method: schedulePreempt(Entity who)",
                    "The Entity given as parameter is a null reference.",
                    "Be sure to have a valid Entity reference for this event to "
                            + "be scheduled with.");
            return; // no proper parameter
        }

        if (!isModelCompatible(who)) {
            sendWarning("Can't schedule Event! Command ignored", "Entity : "
                    + getName() + " Method: schedulePreempt(Entity who)",
                    "The Entity to be scheduled with this event is not "
                            + "modelcompatible.",
                    "Make sure to use compatible model components only.");
            return; // is not compatible
        }
        
        // generate trace
        this.generateTraceForScheduling(who, null, null, null, null, presentTime(), "preempted");
        
        // schedule Event
        getModel().getExperiment().getScheduler().scheduleWithPreempt(who, this);

        if (currentlySendDebugNotes()) {
            sendDebugNote("schedules on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

    }	
}