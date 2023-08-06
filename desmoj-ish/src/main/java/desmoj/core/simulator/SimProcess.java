package desmoj.core.simulator;

import java.util.Enumeration;
import java.util.Vector;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import desmoj.core.advancedModellingFeatures.Res;
import desmoj.core.dist.NumericalDist;
import desmoj.core.exception.DelayedInterruptException;
import desmoj.core.exception.InterruptException;

/**
 * SimProcess represents entities with an own active lifecycle. Since
 * SimProcesses are in fact special entities with extended capabilities (esp.
 * the method <code>lifeCycle()</code>), they inherit from Entity and thus can also
 * be used in conjunction with events. So they can be handled in both ways,
 * event- and process-oriented. Clients are supposed to implement the
 * <code>lifeCycle()</code> method to specify the individual behaviour of a special
 * SimProcess subclass. Since implementing activity- and transaction-oriented
 * synchronization mechanisms requires significant changes in this class,
 * methods that have been implemented by Soenke Claassen have been marked.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Soenke Claassen
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
public abstract class SimProcess extends Entity {

	/**
	 * Displays the current blocked status of this SimProcess. A SimProcess is
	 * blocked whenever it has to wait inside a queue or synchronization object.
	 */
	protected boolean _isBlocked;

	/**
	 * Displays if the thread in of control of this SimProcess is already the
	 * associated simthread. Is <code>true</code> if the simthread is active and
	 * is carrying on its lifeCycle. Is <code>false</code> if it has not started
	 * its lifeCycle yet or is terminated already.
	 */
	protected boolean _isRunning;

	/**
	 * Displays the current status of this SimProcess. Is <code>true</code> if
	 * lifeCycle method has finished, <code>false</code> if it is still running
	 * or has not been started yet
	 */
	protected boolean _isTerminated;

	/**
     * The last Schedulable that has activated this process.
     */	
	private Schedulable _activatedBy;

	/**
	 * The Event which will interrupt the current SimProcess at a given point in
	 * simulation time if it is not removed from the event list before that time
	 * instant.
	 */
	private ExternalEvent _currentlyScheduledDelayedInterruptEvent;
	
    /**
	 * The <code>InterruptCode</code> with which this SimProcess is interrupted.
	 * 
	 * @author Soenke Claassen
	 */

	private InterruptCode _irqCode;

	/**
	 * The <code>InterruptException</code> with which this SimProcess is
	 * interrupted.
	 * 
	 * @author Soenke Claassen
	 */
	private InterruptException _irqException;

	/**
     * Determines whether or not the life cycle of this process will start
     * again after finishing. Default is <code>false</code>, i.e. the lifecycle
     * will only by executed once.
     */ 
    private boolean _isRepeating;

	/**
	 * If this SimProcess is cooperating as a slave with a master process, it
	 * keeps a reference to its master here. Master is set in the
	 * <code>cooperate()</code> -method, when the slave cooperates with his
	 * master and deleted every time the slave process is activated.
	 * 
	 * @author Soenke Claassen
	 */
	private SimProcess _master;

	/**
     * The scheduling priority of the process.
     */
    private int _mySchedulingPriority;

	/**
	 * The Strand (Thread/Fiber) needed for implementing coroutine behaviour.
	 */
	private Strand _myStrand;

	/**
	 * The realTime deadline for this SimProcess in nanoseconds. In case of a
	 * real-time execution (i. e. the execution speed rate is set to a positive
	 * value) the Scheduler will produce a warning message if a deadline is
	 * missed.
	 */
	private long _realTimeConstraint;

	/**
	 * If this SimProcess is cooperating as a slave it has to wait in this
	 * waitQueue until a master is cooperating with it.
	 * 
	 * @author Soenke Claassen
	 */
	private ProcessQueue<? extends SimProcess> _slaveWaitQueue;

	/**
	 * A reference to the container this SimProcess belongs to. Is
	 * <code>null</code> as long as this SimProcess is not contained in any
	 * <code>ComplexSimProcess</code>.
	 * 
	 * @author Soenke Claassen
	 */
	private ComplexSimProcess _supervisor;
	
    /**
	 * The <code>Vector</code> holding all the resources this SimProcess is
	 * using at the moment.
	 * 
	 * @author Soenke Claassen
	 */
	private final Vector<Resource> _usedResources;
	
	/**
     * Short-cut constructor of a SimProcess whose <code>lifeCycle()</code> is only
     * executed once.
     * 
     * @param name
     *            String : The name of the SimProcess
     * @param owner
     *            Model : The model this SimProcess is associated to
     * @param showInTrace
     *            boolean : Flag for showing SimProcess in trace-files. Set it
     *            to <code>true</code> if SimProcess should show up in trace.
     *            Set it to <code>false</code> if SimProcess should not be shown
     *            in trace.
     */
    public SimProcess(Model owner, String name, boolean showInTrace) {

        this(owner, name, false, showInTrace);
    }
	
	/**
	 * The most general constructor of a SimProcess.
	 * 
	 * @param name
	 *            String : The name of the SimProcess
	 * @param owner
	 *            Model : The model this SimProcess is associated to
     * @param repeating
     *            boolean : Flag to set the SimProcess' repeating behaviour:
     *            If set to <code>true</code>, the lifeCycle will be executed 
     *            again after completion, while <code>false</code> will
     *            create a process whose lifeCycle is executed only once.   
	 * @param showInTrace
	 *            boolean : Flag for showing SimProcess in trace-files. Set it
	 *            to <code>true</code> if SimProcess should show up in trace.
	 *            Set it to <code>false</code> if SimProcess should not be shown
	 *            in trace.
	 */
	public SimProcess(Model owner, String name, boolean repeating, boolean showInTrace) {

		super(owner, name, showInTrace);

		// init variables
		_mySchedulingPriority = 0;
		_isBlocked = false; // not waiting in queue so far
		_isRunning = false; // not running so far
		_isTerminated = false; // not terminated either
		_master = null; // this SimProcess has no master, so far
		_slaveWaitQueue = null; // this SimProcess is not waiting in any queue
		_irqCode = null; // this is not interrupted
		_irqException = null; // this is not interrupted
		_isRepeating = repeating;

		// set up the Vector holding the used Resources
		_usedResources = new Vector<Resource>();

		// this SimProcess is not contained in any ComplexSimProcess yet
		_supervisor = null;
	}

	/**
	 * Schedules the SimProcess to be activated at the current simulation time. 
	 * This will allow a passivated SimProcess to resume executing its
     * <code>lifeCycle</code> method. <br/>
     * Process preemption is not permitted: The activated process is resumed
     * <b>after</b> the current process (or other <code>Schedulable</code>) has  
     * transferred program control back to the Scheduler, e.g. by calling <code>hold(...)</code>
     * or <code>passivate()</code>. If process preemption is desired, please use
     * method <code>activatePreempt()</code>.
	 */
	public void activate() {
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: activate()",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		// tell in the trace when the SimProcess will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself now");
			} else { // this is not the currently running SimProcess
				sendTraceNote("activates " + getQuotedName() + " now");
			}
		}
		
		// schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.scheduleNoPreempt(this, null, new TimeSpan(0));

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
     * Schedules the SimProcess to be activated at the given point in
     * simulation time. This will allow a passivated SimProcess to resume executing its
     * <code>lifeCycle</code> method. <br/>
     * Process preemption is not permitted: Even if the argument passed is equal to 
     * <code>presentTime()</code>, the activated process is resumed
     * <b>after</b> the current process (or other <code>Schedulable</code>) has  
     * transferred program control back to the Scheduler, e.g. by calling <code>hold(...)</code>
     * or <code>passivate()</code>. If process preemption is desired, please use
     * method <code>activatePreempt()</code>.
     * 
     * @param when
     *            TimeInstant : The point in simulation time this process is to
     *            be activated.
     */
    public void activate(TimeInstant when) {
        
        if (isBlocked()) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: activate(TimeInstant when)",
                    "The SimProcess to be activated is blocked inside "
                            + "a higher level synchronization object.",
                    "Simprocesses waiting inside higher synchronization "
                            + "constructs can not be activated by other SimProcesses or "
                            + "events!");
            return; // is blocked in some synch construction
        }

        if (when == null) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method:  void activate"
                            + "(TimeInstant when)",
                    "The simulation time given as parameter is a null reference",
                    "Be sure to have a valid simulation time reference before "
                            + "calling this method");
            return; // no proper parameter
        }
        
        if (TimeInstant.isBefore(when, this.presentTime())) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method:  void activate"
                            + "(TimeInstant when)",
                    "The simulation time given as parameter is in the past",
                    "Be sure to have a valid simulation time reference before "
                            + "calling this method");
            return; // no proper parameter
        }
        
        // tell in the trace when the SimProcess will be activated
        if (currentlySendTraceNotes()) {
            if (this == currentSimProcess()) {
                if (TimeInstant.isEqual(when, presentTime())) {
                    sendTraceNote("activates itself now");
                } else {
                    sendTraceNote("activates itself at " + when.toString());

                }
            } else { // this is not the currently running SimProcess

                if (TimeInstant.isEqual(when, presentTime())) {
                    sendTraceNote("activates " + getQuotedName() + " now");
                } else {
                    sendTraceNote("activates " + getQuotedName() + " at "
                            + when.toString());
                }
            }
        }
          
        // schedule this SimProcess
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, null, when);

        // debug output
        if (currentlySendDebugNotes()) {
            sendDebugNote("is activated on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

        resetMaster(); // if activate(TimeInstant when) is called for this
        // SimProcess,
        // there is no Master anymore controlling it.
    }

    /**
	 * Schedules the SimProcess to be activated after a given span of
     * simulation time. This will allow a passivated SimProcess to resume executing its
     * <code>lifeCycle</code> method. <br/>
     * Process preemption is not permitted: Even if the argument passed is equal to 
     * <code>TimeSpan(0)</code>, the activated process is resumed
     * <b>after</b> the current process (or other <code>Schedulable</code>) has  
     * transferred program control back to the Scheduler, e.g. by calling <code>hold(...)</code>
     * or <code>passivate()</code>. If process preemption is desired, please use
     * method <code>activatePreempt()</code>.<br/>
     * Note that for the frequent pattern <code>activate(new TimeSpan(0))</code>,
     * the shorm form <code>activate()</code> exists.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the current simulation time this
	 *            process is to be activated
	 */
	public void activate(TimeSpan dt) {
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: activate(TimeSpan dt)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (dt == null) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method:  void activate"
							+ "(TimeSpan dt)",
					"The simulation time given as parameter is a null reference",
					"Be sure to have a valid simulation time reference before "
							+ "calling this method");
			return; // no proper parameter
		}
		// tell in the trace when the SimProcess will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
                if (dt.isZero()) {
						sendTraceNote("activates itself now");
				} else {
						sendTraceNote("activates itself at "
								+ TimeOperations.add(presentTime(), dt)
										.toString());
				}
			} else { // this is not the currently running SimProcess

				if (dt.isZero()) {
					sendTraceNote("activates " + getQuotedName() + " now");
				} else {
					sendTraceNote("activates "
							+ getQuotedName()
							+ " at "
							+ TimeOperations.add(presentTime(), dt)
									.toString());
				}
			}
		}
		
        // schedule this SimProcess
		getModel().getExperiment().getScheduler().scheduleNoPreempt(this, null, dt);

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.

	}

	/**
	 * Schedules this SimProcess to be activated directly after the given
	 * Schedulable, which itself is already scheduled. Note that this
	 * SimProcess' point of simulation time will be set to be the same as the
	 * Schedulable's time. Thus this SimProcess will continue to execute its
	 * <code>lifeCycle()</code> method directly after the given Schedulable but
	 * the simulation clock will not change. Please make sure that the
	 * Schedulable given as parameter is actually scheduled.
	 * 
	 * @param after
	 *            Schedulable : The Schedulable this SimProcess should be
	 *            scheduled after
	 */
	public void activateAfter(Schedulable after) {

		if (after == null) {
			sendWarning(
					"Can't activate this SimProcess after the given SimProcess "
							+ "parameter! Command ignored.", "SimProcess : "
							+ getName() + " Method: void "
							+ "activateAfter(Schedulable after)",
					"The Schedulable given as parameter is a null reference",
					"Be sure to have a valid Schedulable reference before "
							+ "calling this method");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void activateAfter(Schedulable after)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself after " + getQuotedName());
			} else {
				sendTraceNote("activates " + getQuotedName() + " after "
						+ after.getQuotedName());
			}
		}
	      
        // schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.scheduleAfter(after, this, null);

		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated after " + after.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

	}

	/**
	 * Schedules this SimProcess to be activated directly before the given
	 * Schedulable, which itself is already scheduled. Note that this
	 * SimProcess' point of simulation time will be set to be the same as the
	 * Schedulable's time. Thus this SimProcess will continue to execute its
	 * <code>lifeCycle()</code> method directly before the given Schedulable but
	 * the simulation clock will not change. Please make sure that the
	 * Schedulable given as parameter is actually scheduled.
	 * 
	 * @param before
	 *            Schedulable : The Schedulable this SimProcess should be
	 *            scheduled before
	 */
	public void activateBefore(Schedulable before) {

		if (before == null) {
			sendWarning("Can't activate this SimProcess before the given "
					+ "SimProcess parameter", "SimProcess : " + getName()
					+ " Method: void activateBefore" + "(Schedulable before)",
					"The Schedulable given as parameter is a null reference",
					"Be sure to have a valid Schedulable reference before "
							+ "calling this method");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : "
							+ getName()
							+ " Method: void activateBefore(Schedulable before)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself before "
						+ before.getQuotedName());
			} else {
				sendTraceNote("activates " + getQuotedName() + " before "
						+ before.getQuotedName());
			}
		}
		
        // schedule this SimProcess
		getModel().getExperiment().getScheduler()
				.scheduleBefore(before, this, null);

		if (currentlySendDebugNotes()) {
			sendDebugNote("activateBefore " + before.getQuotedName()
					+ " on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}
		
		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		// if ( isRunning ) passivate();

		resetMaster(); // if activateBefore() is called for this SimProcess,
		// there is no Master anymore controlling it.

	}

	/**
	 * Schedules the SimProcess to be activated immediately at the given point in
	 * simulation time, implicitly stalling the current process until the activated process withdraws
     * program control, referred to as <b>process preemption</b>. 
	 */
	public void activatePreempt() throws SuspendExecution {
	    
		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: activatePreempt()",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}
		
        if (getModel().getExperiment().getScheduler().getCurrentSimProcess() == null) {
            sendWarning("Can't preempt current SimProcess! "
                    + "Command ignored.", "SimProcess : " + getName()
                    + " Method: activatePreempt()",
                    "No current process.",
                    "Call this method during process execution only.");
            return; // preemption of currentprocess only
        }

		// tell in the trace when the SimProcess will be activated
		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("activates itself now (preempted)");
			} else { // this is not the currently running SimProcess
				sendTraceNote("activates " + getQuotedName()
							+ " immediately (preempted)");
			}
		}
		  
        // schedule this SimProcess
		getModel().getExperiment().getScheduler().scheduleWithPreempt(this, null);

		// debug output
		if (currentlySendDebugNotes()) {
			sendDebugNote("is activated on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		resetMaster(); // if activate(TimeInstant when) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
	 * 
	 * Clears the currently scheduled delayed interrupt so that it wont be
	 * performed. This Method should be called to cancel a previously scheduled
	 * delayed interrupt. This is typically the case if all steps to be covered
	 * by the delayed interrupt have been performed in time (before the delayed
	 * interrupt could be executed).
	 * 
	 */
	public void cancelInterruptDelayed() {

		if (isDelayedInterruptScheduled()) {
			sendTraceNote("canceling delayed interrupt scheduled at "
					+ _currentlyScheduledDelayedInterruptEvent.scheduledNext());
			_currentlyScheduledDelayedInterruptEvent.cancel();
			_currentlyScheduledDelayedInterruptEvent = null;
		} else {
			sendWarning(
					"Cannot cancel a delayed interrupt because no delayed interrupt is scheduled. Action ignored.",
					"SimProcess " + getName()
							+ " Method: cancelInterruptDelayed()",
					"No delayed interrupt has been scheduled.",
					"You can use the Method isDelayedInterruptScheduled() on a SimProcess to test whether a delayed interrupt has been scheduled for it.");
		}
	}

	/**
	 * Returns <code>true</code> if this process can cooperate with another
	 * SimProcess. If this process is already cooperating with a master
	 * <code>false</code> is returned.
	 * 
	 * @return boolean : Is this process ready to cooperate with another
	 *         SimProcess?
	 * @author Soenke Claassen
	 */
	public boolean canCooperate() {
		return _master == null; // if the master is not set yet this SimProcess
		// can cooperate with another SimProcess
	}

	/**
	 * Resets the interrupt-status of this SimProcess to not interrupted. Should
	 * be called every time the SimProcess has successfully dealt with the
	 * interrupt. The internal <code>InterruptCode</code> of this SimProcess
	 * will be reset to <code>null</code>.
	 * 
	 * @author Soenke Claassen
	 */
	public void clearInterruptCode() {
		_irqCode = null;
	}

	/**
	 * The current (master) process is calling this method (within
	 * <code>WaitQueue.cooperate()</code>) on the slave process to lead him
	 * through the joint cooperation. After the joint cooperation is finished
	 * the master is still active and after him the slave will be activated.
	 * 
	 * @author Soenke Claassen
	 */
	public void cooperate() {
		// this is the slave and current the master

		// check if this slave already has a master
		if (_master != null) {
			sendWarning(
					"Slaves can not cooperate with more than one master at a "
							+ "time! The attempted cooperation is ignored.",
					"SimProcess : " + getName() + " Method: cooperate () ",
					"This slave process is already cooperating with another "
							+ "master: " + _master.getName(),
					"Be sure to have finished one cooperation before starting "
							+ "the next one.");
			return; // this process has a master already
		}

		// check if this slave is not terminated yet
		if (_isTerminated) {
			sendWarning(
					"Attempt to cooperate with a terminated slave process! "
							+ "The attempted cooperation is ignored.",
					"SimProcess : " + getName() + " Method: cooperate () ",
					"This slave process is already terminated.",
					"Make sure not to cooperate with terminated processes.");
			return; // this process is already terminated
		}

		// check the master
		SimProcess currentMaster = currentSimProcess(); // the current master
		// process
		if (currentMaster == null) // if currentMaster is only a null pointer
		{
			sendWarning("A non existing process is trying to cooperate as a "
					+ "master! The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"The master process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating with other "
							+ "processes. ");
			return; // the currentMaster process is only a null pointer
		}

		if (!isModelCompatible(currentMaster)) {
			sendWarning(
					"The given master SimProcess object does not "
							+ "belong to this model. The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"The master SimProcess is not modelcompatible.",
					"Make sure that the processes all belong to this model.");
			return; // the currentMaster is not modelcompatible
		}

		// the slave must be waiting in a WaitQueue
		if (_slaveWaitQueue == null) {
			sendWarning(
					"Attempt to cooperate with a slave process, that is not "
							+ "waiting in a WaitQueue. The attempted cooperation is ignored!",
					"SimProcess : " + getName() + " Method: cooperate ()",
					"Slave processes must wait in a WaitQueue before they can get into "
							+ "a cooperation.",
					"Make sure that the slave processes are waiting in a WaitQueue.");
			return; // the slave is not waiting in a queue
		}

		// now prepare for the real cooperation
		_master = currentMaster; // set the master for this slave process

		// leave a note in the trace
		if (_master.currentlySendTraceNotes()) {
			// trace note for a cooperation without any special conditions
			sendTraceNote("cooperates " + this.getQuotedName() + " from "
					+ _slaveWaitQueue.getQuotedName());
		}

		// get this slave out of his slaveWaitQueue
		_slaveWaitQueue.remove(this);
		// this slave process is not waiting in any slaveWaitingQueue anymore
		_slaveWaitQueue = null;
		// and therefore this slave process is not blocked anymore
		_isBlocked = false;

	}

	/**
     * Returns the last <code>Schedulable</code> that did cause the last
     * activation (or interruption) of this SimProcess.
     * 
     * @return Schedulable : The Schedulable (e.g. other SimProcess, Event...)
     *         that has caused the last activation of this SimProcess.
     *         As processes may activate themselves, e.g. though a <code>hold(TimeSpan t)</code>
     *         or an <code>activate(TimeSpan t)</code>, this method may return 
     *         a reference to this process. The method returns <code>null</code> if
     *         this process has not yet been activated.
     */
    public Schedulable getActivatedBy() {

        return _activatedBy;
    }

	/**
	 * Returns the InterruptCode from this SimProcess. If this SimProcess is not
	 * interrupted, the InterruptCode is <code>null</code>.
	 * 
	 * @return irqCode : The InterruptCode of this SimProcess.
	 * @author Soenke Claassen
	 */
	public InterruptCode getInterruptCode() {
		return _irqCode;
	}

	public InterruptException getInterruptException() {
		return _irqException;
	}

	/**
	 * Returns the master when two SimProcesses are cooperating. If this method
	 * is called on a SimProcess which is not a slave <code>null</code> is
	 * returned.
	 * 
	 * @return SimProcess : The master process during the cooperation or
	 *         <code>null</code> if this process is not a slave process.
	 * @author Soenke Claassen
	 */
	public SimProcess getMaster() {
		return _master;
	}

	/**
	 * Returns the realTime deadline for this SimProcess (in nanoseconds). In
	 * case of a real-time execution (i. e. the execution speed rate is set to a
	 * positive value) the Scheduler will produce a warning message if a
	 * deadline is missed.
	 * 
	 * @return the realTimeConstraint in nanoseconds
	 */
	public long getRealTimeConstraint() {
		return _realTimeConstraint;
	}

	/**
     * Returns the process' scheduling priority. The scheduling priority is used
     * to determine which process to execute first if two or more processes are activated
     * at the same instant. The default priority is zero.
     * Higher priorities are positive, lower priorities negative.
     * 
     * @return int : The process' priority
     */
    public int getSchedulingPriority() {

        return _mySchedulingPriority;

    }

	/**
	 * Returns the waiting-queue in which this SimProcess is waiting as a slave
	 * to cooperate with a master. If this method is called on a SimProcess
	 * which is not a slave <code>null</code> is returned.
	 * 
	 * @return ProcessQueue : The waiting-queue in which this SimProcess is
	 *         waiting as a slave or <code>null</code> if this SimProcess is not
	 *         waiting as a slave for cooperation.
	 * @author Soenke Claassen
	 */
	public ProcessQueue<? extends SimProcess> getSlaveWaitQueue() {
		return _slaveWaitQueue;
	}

	/**
	 * Returns the supervising <code>ComplexSimProcess</code> this SimProcess is
	 * contained in.
	 * 
	 * @return ComplexSimProcess : The supervising
	 *         <code>ComplexSimProcess</code> this SimProcess is contained in.
	 *         Is <code>null</code> if this SimProcess is not contained in any
	 *         <code>ComplexSimProcess</code>.
	 * @author Soenke Claassen
	 */
	public ComplexSimProcess getSupervisor() {

		return _supervisor;
	}

	/**
     * Passivates a SimProcess for span of time sampled from the distribution
     * provided to the method. The sample is interpreted in the reference time 
     * unit. The SimThread of this SimProcess is put into a lock and the scheduler, resp. the
     * experiment's main thread is released from its block and continues with
     * the next EventNote to be processed.
     * 
     * @param dist
     *            NumericalDist<?> : Numerical distribution to sample the 
     *            duration of the SimProcess' passivation from
     */
    public void hold(NumericalDist<?> dist) throws DelayedInterruptException,
            InterruptException, SuspendExecution {
        
        if ((dist == null)) {
            sendWarning("Can't schedule SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: void hold(NumericalDist<?> dist)",
                    "The NumericalDist given as parameter is a null reference.",
                    "Be sure to have a valid NumericalDist reference before calling this method.");
            return; // no proper parameter
        }

        if (isBlocked()) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method: hold(NumericalDist<?> dist)",
                    "The SimProcess to be activated is blocked inside "
                            + "a higher level synchronization object.",
                    "Simprocesses waiting inside higher synchronization "
                            + "constructs can not be set to be activated by other "
                            + "SimProcesses or events!");
            return; // is blocked in some synch construction
        }

        if (isScheduled()) {
            sendWarning("Can't schedule SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: void hold(NumericalDist<?> dist)",
                    "The SimProcess to be scheduled is already scheduled.",
                    "Use method reActivate(TimeSpan dt) to shift the SimProcess "
                            + "to be scheduled at some other point of time.");
            return; // was already scheduled
        }
                
        // determine time span
        TimeSpan dt = dist.sampleTimeSpan();

        if (currentlySendTraceNotes()) {
            if (this == currentSimProcess()) {
                sendTraceNote("holds for " + dt.toString() + " until "
                        + TimeOperations.add(presentTime(), dt).toString() + " as sampled from " + dist.getQuotedName());
            } else {
                sendTraceNote("holds " + getQuotedName() + "for "
                        + dt.toString() + " until "
                        + TimeOperations.add(presentTime(), dt).toString() + " as sampled from " + dist.getQuotedName());
            }
            skipTraceNote(); // skip passivate message
        }

        // schedule to be reactivated in dt
        getModel().getExperiment().getScheduler().scheduleNoPreempt(this, null, dt);
        
        if (currentlySendDebugNotes()) {
            sendDebugNote("holds on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

        // hand control over to scheduler only if this is
        // a running thread of SimProcess
        passivate();
    }
	
    /**
	 * Passivates a SimProcess until the given point in simulation time. The
	 * simthread of this SimProcess is put into a lock and the scheduler, resp.
	 * the experiment's main thread is released from its block and continues
	 * with the next event-note to be processed.
	 * 
	 * @param until
	 *            TimeInstant : The point in simulation time when the
	 *            SimProcess' passivation ends.
	 * 
	 */
	public void hold(TimeInstant until) throws DelayedInterruptException,
			InterruptException, SuspendExecution {
		if ((until == null)) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The TimeInstant given as parameter is a null reference.",
					"Be sure to have a valid TimeInstant reference before calling this method.");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: hold(TimeInstant until)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be set to be activated by other "
							+ "SimProcesses or events!");
			return; // is blocked in some synch construction
		}

		if (isScheduled()) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The SimProcess to be scheduled is already scheduled.",
					"Use method reActivate(TimeInstant when) to shift the SimProcess "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (TimeInstant.isBefore(until, presentTime())) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeInstant until)",
					"The instant given is in the past.",
					"To hold a SimProcess, use a TimeInstant no earlier than the present time. "
							+ "The present time can be obtained using the "
							+ "presentTime() method.");
			return; // do not hold
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("holds until " + until.toString());
			} else {
				sendTraceNote("holds " + getQuotedName() + "until "
						+ until.toString());
			}
			skipTraceNote(); // skip passivate message
		}

		// schedule to be reactivated at the point of simulation time "until"
		getModel().getExperiment().getScheduler().scheduleNoPreempt(this, null, until);
		
        if (currentlySendDebugNotes()) {
			sendDebugNote("holds on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		passivate();
	}
    
    /**
	 * Passivates a SimProcess for the given span of time. The simthread of
	 * this SimProcess is put into a lock and the scheduler, resp. the
	 * experiment's main thread is released from its block and continues with
	 * the next EventNote to be processed.
	 * 
	 * @param dt
	 *            TimeSpan : The duration of the SimProcess' passivation
	 */
	public void hold(TimeSpan dt) throws DelayedInterruptException,
			InterruptException, SuspendExecution {
		if ((dt == null)) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeSpan dt)",
					"The TimeSpan given as parameter is a null reference.",
					"Be sure to have a valid TimeSpan reference before calling this method.");
			return; // no proper parameter
		}

		if (isBlocked()) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: hold(TimeSpan dt)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be set to be activated by other "
							+ "SimProcesses or events!");
			return; // is blocked in some synch construction
		}

		if (isScheduled()) {
			sendWarning("Can't schedule SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: void hold(TimeSpan dt)",
					"The SimProcess to be scheduled is already scheduled.",
					"Use method reActivate(TimeSpan dt) to shift the SimProcess "
							+ "to be scheduled at some other point of time.");
			return; // was already scheduled
		}

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("holds for " + dt.toString() + " until "
						+ TimeOperations.add(presentTime(), dt).toString());
			} else {
				sendTraceNote("holds " + getQuotedName() + "for "
						+ dt.toString() + " until "
						+ TimeOperations.add(presentTime(), dt).toString());
			}
			skipTraceNote(); // skip passivate message
		}

		// schedule to be reactivated in dt
		getModel().getExperiment().getScheduler().scheduleNoPreempt(this, null, dt);
		
        if (currentlySendDebugNotes()) {
			sendDebugNote("holds on EventList<br>"
					+ getModel().getExperiment().getScheduler().toString());
		}

		// hand control over to scheduler only if this is
		// a running thread of SimProcess
		passivate();
	}

	/**
	 * Interrupts the SimProcess setting the given InterruptCode as the reason
	 * for the interruption. Blocked, terminated or already interrupted
	 * SimProcesses can not be interrupted. In this case a warning message will
	 * be produced and the interrupt will be ignord. If the SimProcess is
	 * cooperating as a slave the interrupt will be passed to the master.
	 * 
	 * @param interruptReason
	 *            desmoj.InterruptCode
	 */
	public void interrupt(InterruptCode interruptReason) {

		if (interruptReason == null) {
			sendWarning(
					"Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"The InterruptCode given as parameter is a null reference.",
					"Be sure to have a valid InterruptCode reference before "
							+ "calling this method.");
			return; // no proper parameter
		}

		// if the SimProcess is cooperating as a slave
		if (_master != null) {
			if (currentlySendTraceNotes()) {
				sendTraceNote("interrupts '" + this.getName() + "' , who ...");
			}

			// interrupt the master, too. (with the same reason/InterruptCode)
			_master.interrupt(interruptReason);
		}

		if (isBlocked()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Blocked SimProcesses can not be interrupted.",
					"You can check if a SimProcess is blocked using method "
							+ "isBlocked().");
			return; // is Blocked
		}

		if (isTerminated()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Terminated SimProcesses can not be interrupted.",
					"You can check if a SimProcess is terminated using method "
							+ "isTerminated().");
			return; // is Terminated
		}

		if (isInterrupted()) {
			sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted("SimProcess : "
					+ getName()
					+ " Method: void "
					+ "interrupt(InterruptCode interruptReason)");
			return;
		}

		if (this == currentSimProcess()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"SimProcess is the currently active SimProcess.",
					"Make sure not to interrupt the currently active "
							+ "SimProcess.");
			return; // is currentSimProcess
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("interrupts '" + this.getName() + "', with reason "
					+ interruptReason.getName() + " ["
					+ interruptReason.getCodeNumber() + "]");
		}
		
        _irqCode = interruptReason; // set the InterruptCode

		// if on EventList, remove first ...
		if (isScheduled()) {
			skipTraceNote(2);
			cancel();
		} else {
			skipTraceNote();
		}
		// ... then activate after the one interrupting this SimProcess
		activateAfter(current());
	}

	/**
	 * Interrupts the SimProcess by throwing the given InterruptException in
	 * it's lifeCylce() method. The InterruptException contains an InterruptCode
	 * as the reason for the interruption. Blocked, terminated or already
	 * interrupted SimProcesses can not be interrupted. In this case a warning
	 * message will be produced and the interrupt will be ignord. If the
	 * SimProcess is cooperating as a slave the interrupt will be passed to the
	 * master.
	 * 
	 * @param interruptReason
	 *            desmoj.InterruptException
	 */
	public void interrupt(InterruptException interruptReason) {
		if (interruptReason == null
				|| interruptReason.getInterruptCode() == null) {
			sendWarning(
					"Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"Either the InterruptException given as parameter or the InterruptCode contained in that Exception is a null reference.",
					"Be sure to have a valid InterruptCode reference before "
							+ "calling this method.");
			return; // no proper parameter
		}

		// if the SimProcess is cooperating as a slave
		if (_master != null) {
			if (currentlySendTraceNotes()) {
				sendTraceNote("interrupts '" + this.getName() + "' , who ...");
			}

			// interrupt the master, too. (with the same reason/InterruptCode)
			_master.interrupt(interruptReason);
		}

		if (isBlocked()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptCode interruptReason)",
					"Blocked SimProcesses can not be interrupted.",
					"You can check if a SimProcess is blocked using method "
							+ "isBlocked().");
			return; // is Blocked
		}

		if (isTerminated()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"Terminated SimProcesses can not be interrupted.",
					"You can check if a SimProcess is terminated using method "
							+ "isTerminated().");
			return; // is Terminated
		}

		if (isInterrupted()) {
			sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted("SimProcess : "
					+ getName()
					+ " Method: void "
					+ "interrupt(InterruptException interruptReason)");
			return;
		}

		if (this == currentSimProcess()) {
			sendWarning("Can't interrupt SimProcess! Command ignored",
					"SimProcess : " + getName() + " Method: void "
							+ "interrupt(InterruptException interruptReason)",
					"SimProcess is the currently active SimProcess.",
					"Make sure not to interrupt the currently active "
							+ "SimProcess.");
			return; // is currentSimProcess
		}

		if (currentlySendTraceNotes()) {
			sendTraceNote("interrupts '" + this.getName() + "', with reason "
					+ interruptReason.getInterruptCode().getName() + " ["
					+ interruptReason.getInterruptCode().getCodeNumber() + "]");
		}

		_irqException = interruptReason; // set the InterruptException
		
        // if on EventList, remove first ...
		if (isScheduled()) {
			skipTraceNote(2);
			cancel();
		} else {
			skipTraceNote();
		}
		// ... then activate after the one interrupting this SimProcess
		activateAfter(current());
	}

	/**
	 * Schedules this process to be interrupted at the given point in simulation
	 * time. Only one delayed interrupt can be scheduled at a time. If a delayed
	 * interrupt is scheduled after another delayed interrupt has already been
	 * scheduled a warning message will be produced and the new delayed
	 * interrupt will not be scheduled.
	 * 
	 * A delayed Interrupt must be cleared manually by calling
	 * clearInterruptDelayed() on the SimProcess if it wasn't performed.
	 * 
	 * @param when
	 *            The Point in time when the interrupt is to be performed.
	 * 
	 * @return The event which will (at the given point in time) triger the
	 *         interrupt of this process
	 */
	public ExternalEvent interruptDelayed(TimeInstant when) {
		ExternalEvent delayedInterruptEvent;

		// when pr�fen

		if (_currentlyScheduledDelayedInterruptEvent != null) {
			sendWarning(
					"Can't schedule a delayed interrupt of this SimProcess! CommandIgnored",
					"SimProcess: "
							+ getName()
							+ " method: void interruptDelayed(TimeInstant when)",
					"Another delayed interrupt has already been scheduled.",
					"A delayed interrupt may only be scheduled if no other delayed interrupt has been scheduled on that SimProcess."
							+ " Did you maybe forget to clear the last delayed interrupt. Delyaed interrupts aren't cleared automatically but must be cleared manually by a call to the method "
							+ "SimProcess#clearInterruptDelayed().");
			return null;
		}

		delayedInterruptEvent = new ExternalEvent(getModel(),
				"DelayedInterruptEvent", true) {

			@Override
			public void eventRoutine() {
				// Interrupt the SimProcess
				SimProcess.this.interrupt(new DelayedInterruptException(
						new InterruptCode("InternalDelayedInterrupt")));
				// Unset the _currentlyScheduledDelayedInterruptEvent so the
				// user is free to schedule another delyed interrupt.
				_currentlyScheduledDelayedInterruptEvent = null;
			}
		};
		if (currentlySendTraceNotes()) {
			sendTraceNote("scheduling a delayed interrupt at " + when);
		}
		skipTraceNote();
		delayedInterruptEvent.schedule(when);

		if (delayedInterruptEvent.isScheduled()) {
			// If the delayed interrupt event has been scheduled auccessfully
			// save it to an instance variable so it may be unscheduled if
			// necessary.
			_currentlyScheduledDelayedInterruptEvent = delayedInterruptEvent;
		} else {
			// For some reason the delayed interrupt event wasn't scheduled. A
			// warning should already haven been sent. So do nothing.
		}

		return _currentlyScheduledDelayedInterruptEvent;
	}
	
	/**
	 * Schedules this process to be interrupted after the given delay. Only one
	 * delayed interrupt can be scheduled at a time. If a delayed interrupt is
	 * scheduled after another delayed interrupt has already been scheduled a
	 * warning message will be produced and the delayed interrupt will not be
	 * scheduled.
	 * 
	 * A delayed Interrupt must be cleared manually by calling
	 * clearInterruptDelayed() on the SimProcess if it wasn't performed.
	 * 
	 * @param delay
	 *            The delay after which the interrupt is to be performed.
	 * 
	 * @return The event which will (after the given delay) triger the interrupt
	 *         of this process
	 */
	public ExternalEvent interruptDelayed(TimeSpan delay) {
		TimeInstant when;

		when = TimeOperations.add(presentTime(), delay);

		return interruptDelayed(when);
	}

	/**
	 * Returns the current block-status of the SimProcess. If a SimProcess is
	 * blocked, it is waiting inside a queue or synchronization block for it's
	 * release.
	 * 
	 * @return boolean : Is <code>true</code> if SimProcess is blocked,
	 *         <code>false</code> otherwise
	 */
	public boolean isBlocked() {

		return _isBlocked;

	}

	/**
	 * Returns the current component status of this SimProcess. If a SimProcess
	 * is a component of a <code>ComplexSimProcess</code> it is blocked and
	 * passivated. It exists only within the <code>ComplexSimProcess</code>;
	 * it's own lifeCycle is stopped and will only be activated again when it is
	 * removed from the <code>ComplexSimProcess</code>.
	 * 
	 * @return boolean :<code>true</code> if and only if this SimProcess is a
	 *         component (part of) a <code>ComplexSimProcess</code>;
	 *         <code>false</code> otherwise.
	 * @author Soenke Claassen
	 */
	public boolean isComponent() {

		return (_supervisor != null);
	}

	public boolean isDelayedInterruptScheduled() {
		return _currentlyScheduledDelayedInterruptEvent != null;
	}

	/**
	 * Returns the current interrupt-status of this SimProcess. If a SimProcess
	 * is interrupted, it should deal with the interrupt and then call the
	 * <code>clearInterruptCode()</code> -method.
	 * 
	 * @return boolean : Is <code>true</code> if this SimProcess is interrupted,
	 *         <code>false</code> otherwise.
	 * @author Soenke Claassen
	 */
	public boolean isInterrupted() {
		return (_irqCode != null || _irqException != null);
	}

	/**
     * Returns the current repeating-status of the SimProcess. If a SimProcess is
     * repeating, the lifeCycle will be executed again after finishing.
     * 
     * @return boolean : Is <code>true</code> if the lifeCycle of the process
     *         will be repeated finishing, while <code>false</code> indicates
     *         the process will terminate after the lifeCycle is completed. 
     */
    public boolean isRepeating() {

        return _isRepeating;

    }
	
	/**
	 * Returns the current status of the SimProcess. If a SimProcess is
	 * terminated, it has already finished its <code>lifeCycle()</code> method
	 * and can not further be used as a SimProcess. A terminated SimProcess can
	 * still be used like any other Entity which it is derived from.
	 * 
	 * @return boolean : Is <code>true</code> if the SimProcess is terminated,
	 *         <code>false</code> otherwise
	 * @see Entity
	 */
	public boolean isTerminated() {

		return _isTerminated;

	}

	/**
	 * Override this method in a subclass of SimProcess to implement the
	 * specific behaviour of this SimProcess. This method starts after a SimProcess
	 * has been created and activated. Note that this method will be executed once 
	 * or repeatedly, depending on the repeating status of the SimProcess.  
	 */
	public abstract void lifeCycle() throws SuspendExecution;

	/**
	 * Makes the SimProcess obtain an array of resources and store them for
	 * further usage.
	 * 
	 * @param obtainedResources
	 *            Resource[] : The array of resources obtained.
	 * 
	 * @author Soenke Claassen
	 */
	public void obtainResources(Resource[] obtainedResources) {
		if (obtainedResources.length <= 0) {
			sendWarning("Attempt to obtain resources, but got none! Command "
					+ "ignored!", "SimProcess : " + getName()
					+ " Method:  void obtain"
					+ "Resources(Resource[] obtainedResources)",
					"The array of obtained resources is empty.",
					"Make sure to obtain at least one resource. Check if the "
							+ "resource pool can provide any resources.");
			return; // parameter contains nothing
		}

		// put all the obtained resources in the Vector of used resources
		for (Resource obtainedResource : obtainedResources) {
			_usedResources.addElement(obtainedResource);
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string of all resources used by this SimProcess
			String t = "uses: ";

			for (Resource resource : _usedResources) {
				t += "<br>" + (resource).getName();
			}

			sendDebugNote(t);
		}
	}

	/**
	 * Passivates the SimProcess for an indefinite time. This method must be
	 * called by the SimProcess' own Thread only. The SimProcess can only be
	 * reactivated by another SimProcess or Entity.
	 */
	public void passivate() throws DelayedInterruptException,
			InterruptException, SuspendExecution {

		if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				sendTraceNote("passivates");
			} else {
				sendTraceNote("passivates " + getQuotedName());
			}
		}

		//wait and free the scheduler
		Strand.parkAndUnpark(this.getModel().getExperiment().getSchedulerStrand());

		// if simulation is not running, throw SimFinishedException to stop
		// thread
		if (getModel().getExperiment().isAborted()) {
			throw (new desmoj.core.exception.SimFinishedException(getModel(),
					getName(), presentTime()));
		}

		if (_irqException != null) {
			// The SimProcess has been interrupted. First reset the
			// _irqException, then throw the exception so that it can be caught
			// in the lifeCycle() method of this SimProcess.

			if (currentlySendTraceNotes()) {
				sendTraceNote("throwing "
						+ _irqException.getClass().getSimpleName()
						+ " to interrupt the process");
			}

			InterruptException tmpIrqException = _irqException;
			_irqException = null;

			throw tmpIrqException;
		}
	}

	/**
	 * The current (master) process is calling this method (within
	 * <code>TransportJunction.cooperate()</code>) on the slave process to make
	 * him prepare for the transportation. After the transport is finished the
	 * master is still active and after him the slave will be activated.
	 * 
	 * @author Soenke Claassen
	 */
	public void prepareTransport() {
		// this is the slave and current the master

		// check if this slave already has a master
		if (_master != null) {
			sendWarning(
					"Slaves can not be transported from more than one master at "
							+ "a time! The attempted transport is ignored.",
					"SimProcess : " + getName()
							+ " Method: prepareTransport () ",
					"This slave process is already transported by another "
							+ "master: " + _master.getName(),
					"Be sure to have finished one transportation before starting "
							+ "the next one.");
			return; // this process has a master already
		}

		// check if this slave is not terminated yet
		if (_isTerminated) {
			sendWarning("Attempt to transport a terminated slave process! "
					+ "The attempted transport is ignored.", "SimProcess : "
					+ getName() + " Method: prepareTransport () ",
					"This slave process is already terminated.",
					"Make sure not to transport terminated processes.");
			return; // this process is already terminated
		}

		// check the master
		SimProcess currentMaster = currentSimProcess(); // the current master
		// process
		if (currentMaster == null) // if currentMaster is only a null pointer
		{
			sendWarning(
					"A non existing process is trying to transport other "
							+ "processes as a master! The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"The master process is only a null pointer.",
					"Make sure that only real SimProcesses are transporting other "
							+ "processes. ");
			return; // the currentMaster process is only a null pointer
		}

		if (!isModelCompatible(currentMaster)) {
			sendWarning(
					"The given master SimProcess object does not "
							+ "belong to this model. The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"The master SimProcess is not modelcompatible.",
					"Make sure that the processes all belong to this model.");
			return; // the currentMaster is not modelcompatible
		}

		// the slave must be waiting in a WaitQueue
		if (_slaveWaitQueue == null) {
			sendWarning(
					"Attempt to transport a slave process, that is not "
							+ "waiting in a TransportJunction. The attempted transport is ignored!",
					"SimProcess : " + getName()
							+ " Method: prepareTransport ()",
					"Slave processes must wait in a TransportJunction before they can be "
							+ "transported.",
					"Make sure that the slave processes are waiting in a "
							+ "TransportJunction.");
			return; // the slave is not waiting in a queue
		}

		// now prepare for the real cooperation
		_master = currentMaster; // set the master for this slave process

		// leave a note in the trace
		if (_master.currentlySendTraceNotes()) {
			// trace note for a transport without any special conditions
			sendTraceNote("transports " + this.getQuotedName() + " from "
					+ _slaveWaitQueue.getQuotedName());
		}

		// get this slave out of his slaveWaitQueue
		_slaveWaitQueue.remove(this);
		// this slave process is not waiting in any slaveWaitingQueue anymore
		_slaveWaitQueue = null;
		// and therefore this slave process is not blocked anymore
		_isBlocked = false;

	}

	/**
     * Re-schedules the activation of this process (which must be exaxctly one)
     * by setting it to the TimeInstant passed to this method. No preemption.
     * 
     * @param when
     *            TimeInstant : The point in simulation time this process is to
     *            be reactivated.
     */
    public void reActivate(TimeInstant when) {
        
        if (isBlocked()) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: reActivate(TimeInstant when)",
                    "The SimProcess to be activated is blocked inside "
                            + "a higher level synchronization object.",
                    "Simprocesses waiting inside higher synchronization "
                            + "constructs can not be activated by other SimProcesses or "
                            + "events!");
            return; // is blocked in some synch construction
        }

        if (when == null) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method:  void reActivate"
                            + "(TimeInstant when)",
                    "The simulation time given as parameter is a null reference",
                    "Be sure to have a valid simulation time reference before "
                            + "calling this method");
            return; // no proper parameter
        }
                
        if (TimeInstant.isBefore(when, this.presentTime())) {
            sendWarning(
                    "Can't activate SimProcess! Command ignored.",
                    "SimProcess : " + getName() + " Method:  void reActivate"
                            + "(TimeInstant when)",
                    "The simulation time given as parameter is in the past",
                    "Be sure to have a valid simulation time reference before "
                            + "calling this method");
            return; // no proper parameter
        }
        
        if (!isScheduled()) {
            sendWarning("Can't reactivate SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: reActivate(TimeInstant when)",
                    "The SimProcess to be reactivated is not scheduled.",
                    "Use method activate(TimeInstant when) to activate a SimProcess"
                            + "that is not scheduled yet.");
            return; // was already scheduled
        }
        
        if (this.getEventNotes().size() > 1) {
            sendWarning("Can't reactivate SimProcess! Command ignored.",
                    "SimProcess : " + getName()
                            + " Method: reActivate(TimeInstant when)",
                    "The SimProcess is scheduled multiple (" + this.getEventNotes().size() + ") times.",
                    "Use method reactivate(TimeInstant when) is only permitted for"
                            + "processes scheduled exactly once.");
            return; // was already scheduled
        }
        
        // tell in the trace when the SimProcess will be activated
        if (currentlySendTraceNotes()) {
            if (this == currentSimProcess()) {
                if (TimeInstant.isEqual(when, presentTime())) {
                    sendTraceNote("reactivates itself now");
                } else {
                    sendTraceNote("reactivates itself at " + when.toString());

                }
            } else { // this is not the currently running SimProcess

                if (TimeInstant.isEqual(when, presentTime())) {
                    sendTraceNote("reactivates " + getQuotedName() + " now");
                } else {
                    sendTraceNote("reactivates " + getQuotedName() + " at "
                            + when.toString());
                }
            }
        }
          
        // schedule this SimProcess
        getModel().getExperiment().getScheduler().reScheduleNoPreempt(this, when);

        // debug output
        if (currentlySendDebugNotes()) {
            sendDebugNote("is activated on EventList<br>"
                    + getModel().getExperiment().getScheduler().toString());
        }

        resetMaster(); // if activate(TimeInstant when) is called for this
        // SimProcess,
        // there is no Master anymore controlling it.
    }

	/**
	 * Re-schedules all SimProcess activation by adding a TimeSpan to the
	 * current activation times.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the current activation times 
	 *            to be added to all activation times.
	 */
	public void reActivate(TimeSpan dt) {
		if (isBlocked()) {
			sendWarning(
					"Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: reActivate(TimeSpan dt)",
					"The SimProcess to be activated is blocked inside "
							+ "a higher level synchronization object.",
					"Simprocesses waiting inside higher synchronization "
							+ "constructs can not be activated by other SimProcesses or "
							+ "events!");
			return; // is blocked in some synch construction
		}

		if (!isScheduled()) {
			sendWarning("Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName()
							+ " Method: reActivate(TimeSpan dt)",
					"The SimProcess to be reactivated is not scheduled.",
					"Use method activate(TimeSpan dt) to activate a SimProcess"
							+ "that is not scheduled yet.");
			return; // was already scheduled
		}

		if (dt == null) {
			sendWarning(
					"Can't reactivate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method:  void reActivate"
							+ "(TimeSpan dt)",
					"The simulation time given as parameter is a null reference",
					"Be sure to have a valid simulation time reference before "
							+ "calling this method");
			return; // no proper parameter
		}
		
        if (currentlySendTraceNotes()) {
			if (this == currentSimProcess()) {
				if (dt.isZero()) {
					sendTraceNote("reactivates itself now");
				} else {
					sendTraceNote("reactivates itself at "
							+ TimeOperations.add(presentTime(), dt));
				}
			} else {
                if (dt.isZero()) {
					sendTraceNote("reactivates " + getQuotedName() + " now");
				} else {
					sendTraceNote("reactivates " + getQuotedName() + " at "
							+ TimeOperations.add(presentTime(), dt));
				}
			}
		}

		getModel().getExperiment().getScheduler().reScheduleNoPreempt(this, dt);

		resetMaster(); // if reActivate(TimeSpan dt) is called for this
		// SimProcess,
		// there is no Master anymore controlling it.
	}

	/**
	 * Gets the InterruptCode from the master and resets the master to
	 * <code>null</code>.
	 * 
	 * 
	 * @author Soenke Claassen
	 */
	public void resetMaster() {
		if (_master != null) {
			_irqCode = _master.getInterruptCode();
			_irqException = _master.getInterruptException();
		}

		_master = null;
	}

	/**
	 * Makes the SimProcess return all resources it holds at the moment to all
	 * the different Res pools it is holding resources from. This is useful in
	 * situations the Simprocess is about to terminate.
	 * 
	 * @author Soenke Claassen
	 */
	public void returnAllResources() {
		// check if something can be returned
		if (_usedResources.isEmpty()) {
			sendWarning(
					"Attempt to return all resources, but the "
							+ "SimProcess does not hold any resources! Command ignored!",
					"SimProcess : " + getName()
							+ " Method: returnAllResources()",
					"If the SimProcess does not hold any resources it is "
							+ "impossible to return any.",
					"Make sure that the SimProcess holds resources that "
							+ "should be returned!");
			return; // return nothing, go to where you came from
		}

		// repeat while vector of usedResources is not empty
		while (!_usedResources.isEmpty()) {
			// get the first resource and check the Res pool it belongs to
			Res crntResPool = _usedResources.firstElement().getResPool();

			// counter how many resources of that res pool are used
			int n = 1;

			// search the whole vector of usedResources for resources of the
			// current
			// Res pool
			for (int i = 1; i < _usedResources.size(); i++) {
				// is the resource of the desired Res pool?
				if (_usedResources.elementAt(i).getResPool() == crntResPool) {
					n++; // increase the counter
				}
			} // end for-loop

			// make the array to store the resources which will be returned
			Resource[] returningRes = new Resource[n];

			// counter for the index of the array
			int k = 0;

			// collect all the resources from the Vector of usedResources
			for (int j = 0; j < _usedResources.size(); j++) {
				// is the resource of the desired Res pool?
				if ((_usedResources.elementAt(j)).getResPool() == crntResPool) {
					// put res in array
					returningRes[k] = _usedResources.elementAt(j);
					k++; // increase counter of array
				}
				if (k == n) {
					break; // stop the for-loop
				}
			}

			// return the array of resources to the Res pool they belong to
			crntResPool.takeBack(returningRes);

			// remove the returned resources from the vector of usedResources
			for (int m = 0; m < n; m++) // go through the array of
			// returningResources
			{
				// remove each resource that is in the array of
				// returningResources
				_usedResources.removeElement(returningRes[m]);
			}

		} // end while

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			// make a string including all elements of the vector usedResources
			String s = "All resources returned! Contents of vector usedResources: ";

			if (_usedResources.isEmpty()) // anything left ?
			{
				s += "<br>none";
			}

			for (Enumeration<Resource> e = _usedResources.elements(); e
					.hasMoreElements();) {
				s += e.nextElement();
			}

			// send a debugNote representing the state of the vector
			// usedResources
			sendDebugNote(s);
		}

	} // end method returnAllResources

	/**
	 * Makes the SimProcess return a certain number of resources of the given
	 * resource pool.
	 * 
	 * @param resPool
	 *            Res : The resource pool which resources will be returned.
	 * @param n
	 *            int : The number of resources which will be returned.
	 * @return Resource[] : the array containing the resources which will be
	 *         returned.
	 * @author Soenke Claassen
	 */
	public Resource[] returnResources(Res resPool, int n) {
		// check if nothing should be returned
		if (n <= 0) {
			sendWarning(
					"Attempt to return no or a negative number of resources! "
							+ " Command ignored!", "SimProcess : " + getName()
							+ " Method:  Resource[] "
							+ "returnResources(Res resPool, int n)",
					"It makes no sense to return nothing or a negative number "
							+ "of resources.",
					"Make sure to return at least one resource. Only resources "
							+ "which have been obtained once can be returned!");
			return null; // return nothing, go to where you came from
		}

		// check if nothing can be returned
		if (_usedResources.isEmpty()) {
			sendWarning(
					"Attempt to return a number of resources, but the "
							+ "SimProcess does not hold any resources! Command ignored!",
					"SimProcess : " + getName() + " Method:  Resource[] "
							+ "returnResources(Res resPool, int n)",
					"If the SimProcess does not hold any resources it is "
							+ "impossible to return any.",
					"Make sure that the SimProcess holds the resources that "
							+ "should be returned!");
			return null; // return nothing, go to where you came from
		}

		// make the array to store the resources which will be returned
		Resource[] returningRes = new Resource[n];

		// counter for the index of the array
		int j = 0;

		// collect all the resources from the Vector of usedResources
		for (int i = 0; i < _usedResources.size(); i++) {
			// is the resource of the desired kind?
			if ((_usedResources.elementAt(i)).getResPool() == resPool) {
				// put res in array
				returningRes[j] = _usedResources.elementAt(i);
				j++; // increase counter of array
			}
			if (j == n) {
				break; // stop the for-loop
			}
		}

		// for debugging: make a string of all returning resources
		String s = "<b>returns</b> to Res '" + resPool.getName() + "' : ";

		// remove the returning resources from the vector of usedResources
		for (int m = 0; m < j; m++) // go through the array of
		// returningResources
		{
			// remove each resource that is in the array of returningResources
			_usedResources.removeElement(returningRes[m]);

			// add them to string of returning resources
			s += "<br>" + returningRes[m].getName();
		}

		if (j < n) // array is not full
		{
			sendWarning("Attempt to return " + n
					+ " resources to the Res pool. "
					+ "But the SimProcess holds only" + j
					+ "resources of that " + "kind. The " + j
					+ "resources will be returned.", "SimProcess : "
					+ getName() + " Method:  Resource[] "
					+ "returnResources(Res resPool, int n)",
					"The SimProcess can not return " + n + " resources, "
							+ "because it holds only" + j + "resources.",
					"Make sure that the SimProcess holds at least as many "
							+ "resources as it should return.");
		}

		// for debugging purposes
		if (currentlySendDebugNotes()) {
			sendDebugNote(s);

			// make a string of all resources still held by this SimProcess
			String t = "still holds: ";

			if (_usedResources.isEmpty()) // anything left ?
			{
				t += "<br>none";
			}

			for (Resource resource : _usedResources) {
				t += "<br>" + (resource).getName();
			}

			sendDebugNote(t);
		}

		return returningRes; // return the array of resources
	}
	

    /**
	 * Sets the SimProcess' blocked status to the boolean value given. This is
	 * necessary for some operations in conjunction with some synchronization
	 * classes.
	 * 
	 * @param blockStatus
	 *            boolean : The new value for the blocked status
	 */
	public void setBlocked(boolean blockStatus) {

		_isBlocked = blockStatus;

	}

	/**
	 * Sets the realTime deadline for this SimProcess (in nanoseconds). In case
	 * of a real-time execution (i. e. the execution speed rate is set to a
	 * positive value) the Scheduler will produce a warning message if a
	 * deadline is missed.
	 * 
	 * @param realTimeConstraint
	 *            the realTimeConstraint in nanoseconds to set
	 */
	public void setRealTimeConstraint(long realTimeConstraint) {
		_realTimeConstraint = realTimeConstraint;
	}

	/**
     * Sets the SimProcess' repeating status to the boolean value given, permitting
     * the process' lifeCycle to either start again (<code>true</code>) or terminate
     * (<code>false</code>) after the current cycle is completed. Note that setting
     * setting this property to <code>true</code> <i>after</i> the the process has
     * terinated has no effect.  
     * 
     * @param repeatingStatus
     *            boolean : The new value for the repeating status
     */
    public void setRepeating(boolean repeatingStatus) {

        _isRepeating = repeatingStatus;

    }
	
    /**
     * Sets the process' scheduling priority to a given integer value. The default 
     * priority (unless assigned otherwise) is zero. 
     * Negative priorities are lower, positive priorities are higher.
     * All values should be inside the range defined by Java's integral
     * <code>integer</code> data type [-2147483648, +2147483647].
     * 
     * An process' scheduling priority it used to determine which process is 
     * executed first if activated at the same time instant.
     * Should the priority be the same, order of event execution depends on the 
     * <code>EventList</code> in use, e.g. activated first is executed 
     * first (<code>EventTreeList</code>) or random (<code>RandomizingEventTreeList</code>). 
     *
     * @param newPriority
     *            int : The new scheduling priority value
     */
    public void setSchedulingPriority(int newPriority) {

        this._mySchedulingPriority = newPriority;

    }


	/**
	 * Sets the SimProcess' slaveWaitQueue variable to the ProcessQueue in
	 * which this SimProcess is waiting as a slave to cooperate with a master.
	 * 
	 * @param slvWaitQueue
	 *            ProcessQueue : The waiting-queue in which this SimProcess is
	 *            waiting as a slave to cooperate with a master.
	 * @author Soenke Claassen
	 */
	public void setSlaveWaitQueue(
			ProcessQueue<? extends SimProcess> slvWaitQueue) {
		_slaveWaitQueue = slvWaitQueue;
	}

	/**
	 * As there is no generally applicable means of cloning a SimProcess (which
	 * would require cloning the execution state as well), this method returns a
	 * <code>CloneNotSupportedException</code>.
	 * 
	 * @return SimProcess : A copy of this process.
	 */
	protected SimProcess clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Returns a clone of the internal <code>Vector</code> containing all the
	 * <code>Resource</code> objects this SimProcess is using at the moment.
	 * 
	 * @return java.util.Vector : the internal <code>Vector</code> containing
	 *         all the <code>Resource</code> objects this SimProcess is using at
	 *         the moment.
	 * @author Soenke Claassen
	 */
	protected Vector<Resource> getUsedResources() {

		// clone the internal Vector
		@SuppressWarnings("unchecked")
		Vector<Resource> usedRes = (Vector<Resource>) _usedResources.clone();

		// return the cloned Vector
		return usedRes;
	}

	/**
	 * Sets the supervising <code>ComplexSimProcess</code> this SimProcess is
	 * contained in. Setting it to <code>null</code> indicates that this
	 * SimProcess is not contained in any <code>ComplexSimProcess</code>
	 * (anymore).
	 * 
	 * @param complexProcess
	 *            desmoj.ComplexSimProcess : The <code>ComplexSimProcess</code>
	 *            which serves as a container for this SimProcess.
	 * @author Soenke Claassen
	 */
	protected void setSupervisor(ComplexSimProcess complexProcess) {

		_supervisor = complexProcess;
	}
	
    /**
	 * Method to release the waiting scheduler when the SimThread finishes.
	 */
	void freeThread() throws SuspendExecution {
	    Strand s = this.getModel().getExperiment().getSchedulerStrand();
	    if (s != null) {
	        s.unpark();
	    }
	}	
	
    /**
     * Returns the current repeating-status of the SimProcess. If a SimProcess is
     * repeating, the lifeCycle will be executed again after finishing.
     * 
     * @return boolean : Is <code>true</code> if the lifeCycle of the process
     *         will be repeated finishing, while <code>false</code> indicates
     *         the process will terminate after the lifeCycle is completed. 
     */

	/**
	 * Returns the current running status of the SimProcess. If a SimProcess
	 * is not ready, it has already finished its <code>lifeCycle()</code> method
	 * and can not further be used as a SimProcess. A terminated SimProcess can
	 * still be used like any other Entity which it is derived from.
	 * 
	 * @return boolean : Is <code>true</code> if the SimProcess is terminated,
	 *         <code>false</code> otherwise
	 * @see Entity
	 */
	boolean isReady() {

		return _isRunning;

	}

	/**
	 * Used to synchronize the change of control between scheduler and
	 * SimProcesses. This method must only be called by the scheduler resp. the
	 * experiment's main thread in order to prevent multiple SimProcess' threads
	 * running in parallel which has to be avoided.
	 */
	void resume() throws SuspendExecution {

		// check that the SimThread has not finished yet
		if (_isTerminated) {
			sendWarning(
					"Can't activate SimProcess! Command ignored.",
					"SimProcess : " + getName() + " Method: void resume()",
					"The SimProcess' lifeCycle method has already terminated.",
					"Be sure to check the SimProcess' status before resuming."
							+ " Use method isTerminated() to check the current status");
			return;
		}

		// wake up the SimThread waiting in a block for the SimProcess' lock
		// to be released
		// and go wait until the next notification by the SimThread
		// of this SimProcess
		Strand.parkAndUnpark(_myStrand);
	}

	/**
     * Sets the last <code>Schedulable</code> which has (re)activated or 
     * interrupted this SimProcess.
     * 
     * @param by
     *            Schedulable : The Schedulable that has (re)activated or 
     *            interrupted this SimProcess or <code>null</code> to un-set
     *            this information. 
     */
    void setActivatedBy(Schedulable by) {

        _activatedBy = by;
    }

	/**
	 * Sets the SimProcess' running status to the boolean value given. This is
	 * necessary for some operations in conjunction with synchronization
	 * classes.
	 * 
	 * @param runStatus
	 *            boolean : The new value for the running status
	 */
	void setRunning(boolean runStatus) {

		_isRunning = runStatus;

	}

	/**
	 * Sets the attribute indicating that this SimProcess' simthread has
	 * finished to the given value. This method is used by class
	 * <code>SimThread<code> only.
	 * 
	 * @param termValue
	 *            boolean : The new value for the attribute indicating the
	 *            SimThread's end
	 */
	void setTerminated(boolean termValue) {

		_isTerminated = termValue; // Hasta la vista, baby!

	}
	
    /**
	 * Starts the simthread associated with this SimProcess. This is method must
	 * be called the first time a SimProcess is supposed to start processing
	 * its <code>lifeCycle()</code> method.
	 */
	void start() throws SuspendExecution {
	    
        // set up simthread
        _myStrand = this.getModel().getExperiment().getStrandFactory().create(this, new SimThread(this));

		// setting this flag shows that the simthread is now ready to take over
		// control from the scheduler's thread
		_isRunning = true;

		// start thread and let it run into the block
		_myStrand.start();

		// put scheduler thread in to a wait for synchronization
		Strand.park();

		// check if simulation has been stopped in between and throw SimFinished
		if (getModel().getExperiment().isAborted()) {
			throw (new desmoj.core.exception.SimFinishedException(getModel(),
					getName(), presentTime()));
		}
	}

    void unpark() throws SuspendExecution {
        _myStrand.unpark();
    }
    
	private void sendAWarningThatTheCurrentSimProcessHasAlreadyBeenInterrupted(
			String location) {
		String reason;

		if (_irqCode != null) {
			reason = "The SimProcess already has an InterruptCode set: "
					+ _irqCode.getName();
		} else if (_irqException != null) {
			reason = "The SimProcess already has an InterruptException set which contains the following InterruptCode: "
					+ _irqException.getInterruptCode().getName();
		} else {
			throw new RuntimeException(
					"Apparently this method has been called although the current process hasn't been interrupted.");
			// This should never happen.
		}

		// is Interrupted
		sendWarning(
				"Can't interrupt SimProcess! Command ignored",
				location,
				reason,
				"SimProcesses may only be interrupted if no other "
						+ "InterruptCode or InterruptException is set on that SimProcess. You can check "
						+ "on that using the mehtod isInterrupted(), which must return "
						+ "false if no other InterruptCode or InterruptException is set.");
	}
} // end class SimProcess