package desmoj.core.simulator;

import java.util.Enumeration;
import java.util.Vector;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.concurrent.ReentrantLock;

/**
 * A <code>ComplexSimProcess</code> is a <code>SimProcess</code> which
 * serves as a container for other <code>SimProcess</code> es. A
 * <code>ComplexSimProcess</code> has its own lifecycle. As long as this
 * <code>ComplexSimProcess</code> is active all its contained Simprocesses are
 * passive. That means they are blocked and can not proceed in their lifeCycles.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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
public abstract class ComplexSimProcess extends SimProcess {

	/**
	 * The <code>Vector</code> holding all the SimProcesses which are
	 * contained in this ComplexSimProcess.
	 */
	private Vector<SimProcess> _components;
	
	/**
	 * The lock for access to the components vector.
	 */
	private ReentrantLock _lock = new ReentrantLock();

	/**
	 * Constructs a ComplexSimProcess.
	 * 
	 * @param owner
	 *            desmoj.Model : The model this ComplexSimProcess is associated
	 *            to.
	 * @param name
	 *            java.lang.String : The name of this ComplexSimProcess.
	 * @param showInTrace
	 *            boolean : Flag for showing trace messages of this
	 *            ComplexSimProcess in trace-files. Set it to <code>true</code>
	 *            if ComplexSimProcess should show up in trace. Set it to
	 *            <code>false</code> if ComplexSimProcess should not be shown
	 *            in trace.
	 */
	public ComplexSimProcess(Model owner, String name, boolean showInTrace) {

		super(owner, name, showInTrace); // make a SimProcess

		// make a new Vector to store all the components in
		_components = new Vector<SimProcess>();

		// this ComplexSimProcess is not contained in any other
		// ComplexSimProcess yet
		setSupervisor(null);
	}

	/**
	 * Adds a SimProcess as a component to this ComplexSimProcess. The
	 * Sim-process being added to the ComplexSimProcess will be passivated and
	 * blocked. Use method <code>removeComponent()</code> to remove the
	 * Sim-process from the ComplexSimProcess again.
	 * 
	 * @param compnt
	 *            desmoj.SimProcess : The SimProcess to be added as a component
	 *            to this ComplexSimProcess.
	 */
	public void addComponent(SimProcess compnt) throws SuspendExecution {

        _lock.lock();
        try {
    		// check the given SimProcess
    		if (compnt == null) // if compnt is a null pointer instead of a process
    		{
    			sendWarning("Attempt to add a non existing process to a "
    					+ "ComplexSimProcess. The attempted action is ignored!",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ " addComponent(SimProcess compnt)",
    					"The given SimProcess is only a null pointer.",
    					"Make sure that only real SimProcesses are added to a  "
    							+ "ComplexSimProcess.");
    
    			return; // ignore that rubbish
    		}
    
    		if (!isModelCompatible(compnt)) // if compnt is not modelcompatible
    		{
    			sendWarning(
    					"The process trying to be added to a ComplexSimProcess does "
    							+ "not belong to this model. The attempted action is ignored!",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ " addComponent(SimProcess compnt)",
    					"The given process belongs to model "
    							+ compnt.getModel().getQuotedName()
    							+ " and therefore is not modelcompatible.",
    					"Make sure that the processes to be added to a ComplexSimProcess belong "
    							+ "to the same model or overwrite the method "
    							+ "<code>isModelCompatible()</code>");
    
    			return; // ignore that rubbish
    		}
    
    		// the Simprocess component must not be contained twice in the container
    		if (_components.contains(compnt)) {
    			sendWarning(
    					"Attempt to add a process to a ComplexSimProcess twice. "
    							+ "The attempted action is ignored!",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ " addComponent(SimProcess compnt)",
    					"The given SimProcess is already a component of this "
    							+ "ComplexSimProcess.",
    					"Make sure the SimProcess is not contained in the "
    							+ "ComplexSimProcess already.");
    			return;
    		}
    
    		// the SimProcess to be added to a ComplexSimProcess should be active or
    		// passivated but not scheduled. Just in case it is scheduled, cancel
    		// that
    		if (compnt.isScheduled()) {
    			sendWarning(
    					"The SimProcess added to a ComplexSimProcess is scheduled! "
    							+ "The scheduled activation of the SimProcess will be "
    							+ "cancelled.",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ " addComponent(SimProcess compnt)",
    					"A SimProcess added to a ComplexSimProcess is giving up its "
    							+ "own lifeCycle and therefore should not be scheduled anymore."
    							+ " It will be carried on by the lifeCycle of the "
    							+ "ComplexSimProcess.",
    					"Make sure that the SimProcess is either adding itself to a "
    							+ "ComplexSimProcess or that the SimProcess is passive.");
    
    			compnt.skipTraceNote();
    			compnt.cancel();
    		}
    
    		// set the supervisor of the SimProcess added
    		compnt.setSupervisor(this);
    
    		// the component SimProcess is blocked
    		compnt.setBlocked(true);
    
    		// add the new SimProcess component to the components Vector
    		_components.addElement(compnt);
    
    		// trace output
    		if (currentlySendTraceNotes()) {
    			sendTraceNote("adds " + compnt.getQuotedName()
    					+ " as a component to " + this.getQuotedName());
    		}
    
    		// debug output
    		if (currentlySendDebugNotes()) {
    			sendDebugNote("adds " + compnt.getQuotedName()
    					+ " to its components." + "it now looks like <br>"
    					+ this.toString());
    		}
    
    		// either the component SimProcess is adding itself to the
    		// ComplexSimProcess
    		// or it is passive already
    		if (currentSimProcess() == compnt) // adds itself to the complex
    		{
    			compnt.skipTraceNote(); // don't tell the user, that we ...
    			compnt.passivate(); // passivate the component process
    		}
        } finally {
            _lock.unlock();
        }
	}

	/**
	 * Checks if the given SimProcess is contained in this ComplexSimProcess
	 * already.
	 * 
	 * @return boolean :<code>true</code> if and only if the specified
	 *         SimProcess is the same as a component in this ComplexSimProcess,
	 *         as determined by the <code>equals()</code> method;
	 *         <code>false</code> otherwise.
	 * @param elem
	 *            desmoj.SimProcess : The SimProcess which might be an element
	 *            of this ComplexSimPorcess already.
	 * @see java.util.Vector
	 */
	public boolean contains(SimProcess elem) {
        _lock.lock();
        try {
    
    		// forward to the internal Vector
    		return _components.contains(elem);
        } finally {
            _lock.unlock();
        }
	}

	/**
	 * Returns all the components of this ComplexSimProcess as an
	 * <code>java.util.Enumeration</code>.
	 * 
	 * @return java.util.Enumeration : All the components of this
	 *         ComplexSimProcess.
	 * @see java.util.Enumeration
	 */
	public Enumeration<SimProcess> getComponents() {
        _lock.lock();
        try {
    
    		return _components.elements();
        } finally {
            _lock.unlock();
        }
	}

	/**
	 * Checks if this ComplexSimProcess has components or not.
	 * 
	 * @return boolean :<code>true</code> if and only if this
	 *         ComplexSimProcess has components; <code>false</code> otherwise.
	 */
	public boolean hasComponents() {

		// forward to the internal Vector
		return (!_components.isEmpty());
	}

	/**
	 * Override this method in a subclass of <code>ComplexSimProcess</code> to
	 * implement the specific behaviour of this process. This method starts
	 * after a <code>ComplexSimProcess</code> has been created and activated
	 * by the scheduler. As long as this <code>ComplexSimProcess</code> is
	 * active all its contained Simprocesses are passive. That means they are
	 * blocked and can not proceed in their lifeCycles.
	 */
	public abstract void lifeCycle() throws co.paralleluniverse.fibers.SuspendExecution;

	/**
	 * Removes all elements (SimProcesses and ComplexSimProcesses) from this
	 * ComplexSimProcess. This will be done for all ComplexSimPorcesses
	 * recursively, until all simple SimProcesses are removed. The SimProcesses
	 * being removed from the ComplexSimProcess will be activated after the
	 * current SimProcess so they can follow their own lifeCycle again. Of
	 * course, this does only make sense for SimProcesses which are not
	 * terminated already.
	 */
	public void removeAllComponents() {

        _lock.lock();
        try {
    		// loop through all elements
    		for (Enumeration<?> e = getComponents(); e.hasMoreElements();) {
    			// buffer the current element
    			SimProcess elem = (SimProcess) e.nextElement();
    
    			// reset the supervisor of the SimProcess element removed
    			elem.setSupervisor(null);
    
    			// the SimProcess element is not blocked anymore
    			elem.setBlocked(false);
    
    			// activate the removed SimProcess (if it is not terminated yet)
    			if (!elem.isTerminated()) {
    				elem.skipTraceNote(); // don't tell the user, that we ...
    				elem.activateAfter(currentSimProcess()); // activate the
    				// process
    				// elem again
    			}
    
    			// if this ComplexSimProcess contains other ComplexSimProcesses
    			// remove them also
    			if (elem instanceof ComplexSimProcess) {
    				((ComplexSimProcess) elem).removeAllComponents();
    			}
    		}
    
    		// remove the all the SimProcess elements from the components Vector
    		_components.removeAllElements();
    
    		// trace output
    		if (currentlySendTraceNotes()) {
    			sendTraceNote("removes all elements from " + this.getQuotedName());
    		}
    
    		// debug output
    		if (currentlySendDebugNotes()) {
    			sendDebugNote("removes all its elements.");
    		}
        } finally {
            _lock.unlock();
        }
	}

	/**
	 * Removes a SimProcess from the elements of this ComplexSimProcess. The
	 * Sim-process being removed from the ComplexSimProcess will be activated
	 * after the current SimProcess so it can follow its own lifeCycle again.
	 * 
	 * @param elem
	 *            desmoj.SimProcess : The SimProcess to be removed from the
	 *            elements of this ComplexSimProcess. Be careful, it might also
	 *            be a <code>ComplexSimProcess</code>.
	 */
	public void removeComponent(SimProcess elem) {

        _lock.lock();
        try {
    		// check the given SimProcess
    		if (elem == null) // if elem is a null pointer instead of a process
    		{
    			sendWarning("Attempt to remove a non existing process from a "
    					+ "ComplexSimProcess. The attempted action is ignored!",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ "removeComponent(SimProcess elem)",
    					"The given SimProcess is only a null pointer.",
    					"Make sure to remove only SimProcesses that are contained in this "
    							+ "ComplexSimProcess.");
    
    			return; // ignore that rubbish
    		}
    
    		// the SimProcess component must be contained in the container
    		if (!_components.contains(elem)) {
    			sendWarning(
    					"Attempt to remove a process which is not an element of this "
    							+ "ComplexSimProcess. The attempted action is ignored!",
    					"ComplexSimProcess: " + getName()
    							+ " Method: synchronized void"
    							+ "removeComponent(SimProcess elem)",
    					"The given SimProcess is not a component of this "
    							+ "ComplexSimProcess.",
    					"Make sure to remove only SimProcesses that are contained in "
    							+ "this ComplexSimProcess.");
    			return;
    		}
    
    		// reset the supervisor of the SimProcess element removed
    		elem.setSupervisor(null);
    
    		// remove the SimProcess element from the components Vector
    		_components.remove(elem);
    
    		// the SimProcess element is not blocked anymore
    		elem.setBlocked(false);
    
    		// activate the removed SimProcess (if it is not terminated yet)
    		if (!elem.isTerminated()) {
    			elem.skipTraceNote(); // don't tell the user, that we ...
    			elem.activateAfter(currentSimProcess()); // activate the process
    			// elem again
    		}
    
    		// trace output
    		if (currentlySendTraceNotes()) {
    			sendTraceNote("removes " + elem.getQuotedName() + " from "
    					+ this.getQuotedName());
    		}
    
    		// debug output
    		if (currentlySendDebugNotes()) {
    			sendDebugNote("removes " + elem.getQuotedName()
    					+ " from its elements." + "it now looks like <br>"
    					+ this.toString());
    		}
        } finally {
            _lock.unlock();
        }
	}

	/**
	 * Returns a <code>String</code> representation of this ComplexSimProcess,
	 * containing the <code>String</code> representation of each
	 * <code>SimProcess</code> component.
	 * 
	 * @return java.lang.String : A <code>String</code> representation of this
	 *         ComplexSimProcess.
	 */
	public String toString() {

		StringBuffer stringOfElems = new StringBuffer();

		stringOfElems.append(this.getQuotedName() + " consists of : ");

		// make the String by collecting the Strings from all elements
		if (!hasComponents()) {
			stringOfElems.append("nothing else.");
		} else {
			// loop through all elements
			for (Enumeration<?> e = getComponents(); e.hasMoreElements();) {
				stringOfElems.append(((SimProcess) e.nextElement())
						.getQuotedName());

				if (e.hasMoreElements()) {
					stringOfElems.append(", ");
				} else {
					stringOfElems.append(".");
				}
			}
		}

		return stringOfElems.toString();
	}
}