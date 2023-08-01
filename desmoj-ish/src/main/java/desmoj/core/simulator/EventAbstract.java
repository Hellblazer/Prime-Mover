package desmoj.core.simulator;

/**
 * Provides the abstract super class for user defined events to change an entity's
 * internal state. The state of a discrete model is changed by events that occur
 * at distinct points of simulation time.
 * <p>
 * an event with up to three entities, changing its state according to the
 * entity's reaction to the specific Event in the system under inspection. So
 * each type of Event acting on one certain type of Entity requires a new
 * subclass to be derived from this class. Since events are associated to a
 * single entity, the method executing the changes of state of a specific Entity
 * gets that entity passed as a parameter. The scheduler takes care that this is
 * done at the specified point of simulation time.
 * <p>
 * For type safety it is recommended to generically assign the entity type an
 * Event operates on by using the generic type
 * <code>Event&lt;EntityOperatingOn&gt;</code> where
 * <code>EntityOperatingOn</code> is derived from <code>Entity</code>.
 * <p>
 * events should be used one time only. They are created to be scheduled
 * together with a specific Entity, change that entity's state at the scheduled
 * point of simulation time and are destroyed by Java's garbage collector after
 * use. They could be reused but at a certain risk of inconsistent states. Since
 * each object of a class that is derived from the class
 * <code>Schedulable</code> has its unique identification number added as a
 * suffix to its name, reusing Event objects would make one event responsible
 * for several distinct changes in a model at different simulation times. This
 * comes with the danger of of confusing the model's trace and making it more
 * difficult to debug a faulty model implementation. Each type of Event needed
 * for a model requires a new subclass of Event to be derived by the user.
 * <p>
 * Embed the changes of state for the specific Entity associated with this event
 * by overriding the abstract method <code>eventRoutine(Entity e)</code>. Events
 * that do not manipulate a single entity but act on the model's state on a more
 * general matter are defined by external events, a subclass of this class.
 * 
 * @see Entity
 * @see ExternalEvent
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
public abstract class EventAbstract extends Schedulable 
{
	/**
	 * The number of entities is saved here.
	 */
	protected long numberOfEntities;
	
	/**
     * The scheduling priority of the event.
     */
    private int _mySchedulingPriority;
	
	/**
	 * The realTime deadline for this event in nanoseconds. In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 */
	private long _realTimeConstraint;

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
	public EventAbstract(Model owner, String name, boolean showInTrace)
	{

		super(owner, name, showInTrace);
		numberOfEntities = 0;
		_mySchedulingPriority = 0;

	}
	
	/**Returns the realTime deadline for this event (in nanoseconds). In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 * 
	 * @return the realTimeConstraint in nanoseconds
	 */
	public long getNumberOfEntities()
	{
		return numberOfEntities;
	}
    

	/**Returns the realTime deadline for this event (in nanoseconds). In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 * 
	 * @return the realTimeConstraint in nanoseconds
	 */
	public long getRealTimeConstraint() {
		return _realTimeConstraint;
	}
	
	/**
     * Returns the event's scheduling priority. The scheduling priority is used
     * to determine which event to execute first if two or more events are scheduled
     * at the same instant. The default priority is zero.
     * Higher priorities are positive, lower priorities negative.
     * 
     * @return int : The events's priority
     */
    public int getSchedulingPriority() {

        return _mySchedulingPriority;

    }
	
	/**
	 * Tests if this event actually is an external event which is not used for
	 * modelling but to control the experiment to act at certain points of
	 * simulation time. External events must not be connected to an entity.
	 * 
	 * @return boolean : Is <code>true</code> if this is an instance of class
	 *         <code>ExternalEvent</code>,<code>false</code> otherwise
	 */
	public boolean isExternal() {

		return (this instanceof ExternalEvent);

	}
	
	/**Sets the realTime deadline for this event (in nanoseconds). In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 * 
	 * @param realTimeConstraint the realTimeConstraint in nanoseconds to set
	 */
	public void setRealTimeConstraint(long realTimeConstraint) {
		this._realTimeConstraint = realTimeConstraint;
	}
	
    /**
     * Sets the entity's scheduling priority to a given integer value. The default 
     * priority (unless assigned otherwise) is zero. 
     * Negative priorities are lower, positive priorities are higher.
     * All values should be inside the range defined by Java's integral
     * <code>integer</code> data type [-2147483648, +2147483647].
     * 
     * An event's scheduling priority it used to determine which event is 
     * executed first if scheduled for the same time instant.
     * Should the priority be the same, event execution depends on the 
     * <code>EventList</code> in use, e.g. scheduled first is executed 
     * first (<code>EventTreeList</code>) or random (<code>RandomizingEventTreeList</code>). 
     *
     * @param newPriority
     *            int : The new scheduling priority value
     */
    public void setSchedulingPriority(int newPriority) {

        this._mySchedulingPriority = newPriority;

    }

    /**
     * Creates and returns a copy of this event.
     * Note that subclasses have to implement the interface 
     * <code>java.lang.Cloneable</code> to actually use this method as 
     * otherwise, a <code>CloneNotSupportedException</code> will be thrown.
     * 
     * @return EventAbstract : A copy of this event.
     */  
    protected EventAbstract clone() throws CloneNotSupportedException {
        return (EventAbstract) super.clone();
    }
	
    /**
	 * Utility method to generate trace output for scheduling this event (internal use only).
	 *
	 * @param who1 the first entity scheduled with this event (or <code>null</code> if not applicable) 
	 * @param who2 the second entity scheduled with this event (or <code>null</code> if not applicable)
	 * @param who3 the third entity scheduled with this event (or <code>null</code> if not applicable)
	 * @param after the Schedulable after which this event is scheduled (or <code>null</code> if not applicable)
	 * @param before the Schedulable before which this event is scheduled (or <code>null</code> if not applicable)
	 * @param at the TimeInstant at which this event is scheduled
	 * @param remark optional further comments (or <code>null</code> if not applicable)
	 */
	protected void generateTraceForScheduling(Entity who1, Entity who2, Entity who3, Schedulable after, Schedulable before, TimeInstant at, String remark) {
       
	    if (currentlySendTraceNotes()) {
            
            StringBuilder trace = new StringBuilder("schedules '" + getName() + "'");
            if (who1 != null) {
                String who1alias = (who1 == currentEntity() && who2 == null && who3 == null? "itself" : "'" + who1.getName() + "'");
                trace.append(" of " + who1alias);
                if (who2 != null) {
                    trace.append(who3 == null ? " and '" + who2.getName() + "'" : ", '" + who2.getName() + "'");
                    if (who3 != null) {
                        trace.append(" and '" + who3.getName() + "'");
                    }
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
                trace.append(" now");
            } else {
                trace.append(" at " + at.toString());
            }
            
            if (remark != null && remark.length() > 0) {
                trace.append(". " + remark);
            }
            
            this.sendTraceNote(trace.toString());
        }
	}
}