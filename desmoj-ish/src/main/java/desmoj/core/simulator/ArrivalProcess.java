package desmoj.core.simulator;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.NumericalDist;

/**
 * An <code>ArrivalProcess</code> is some kind of source for
 * <code>SimProcess</code> es. It makes them arrive from the 'outside world'
 * in the system of interest. So it can be viewed as the border of the system
 * (model). The user has to specify at which rate (frequency)
 * <code>SimProcess</code> es are arriving in the system. See the Constructor
 * for more details. The user has to derive from this class to make his own
 * ArrivalProcesses for the arriving processes he needs in his simulated system.
 * He has to implement the abstract method <code>createSuccessor()</code>,
 * which should create (instantiate) the newly arriving process. It is
 * recommended to use one ArrivalProcess for each different kind of arriving
 * process (with its specific arrival rate).
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author modified by Ruth Meyer
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
public abstract class ArrivalProcess<S extends SimProcess> extends SimProcess
{

	/**
	 * The rate (frequency) at which the SimProcesses arrive at the system
	 * (border).
	 */
	private desmoj.core.dist.NumericalDist<Double> _arrivalRate;

	/**
	 * Constructor for an ArrivalProcess to make a certain kind of SimProcess
	 * arrive automatically in the system at a given rate.
	 * 
	 * @param owner
	 *            desmoj.Model : The model this ArrivalProcess belongs to.
	 * @param name
	 *            java.lang.String : The name of this ArrivalProcess, should
	 *            indicate that this is a source of some kind of SimProcesses.
	 * @param arrivalRate
	 *            desmoj.dist.NumericalDist<Double> : The rate at which the processes are
	 *            arriving at the system.
	 * @param showInTrace
	 *            boolean : Flag, if this ArrivalProcess should produce a trace
	 *            output or not.
	 */
	public ArrivalProcess(Model owner, String name, NumericalDist<Double> arrivalRate,
			boolean showInTrace) {
		super(owner, name, true, showInTrace); // make a SimProcess

		this._arrivalRate = arrivalRate;
	}

	/**
	 * Implement this abstract method so it creates (instantiates) a new
	 * <code>SimProcess</code> object which is arriving at the system
	 * (border). When this method returns <code>null</code> the arrival
	 * process stops its lifecycle.
	 * 
	 * @return desmoj.core.SimProcess : The <code>SimProcess</code> object which is
	 *         arriving next in the system.
	 */
	public abstract S createSuccessor();

	/**
	 * Returns the rate (frequency) at which the SimProcesses arrive at the
	 * system.
	 * 
	 * @return desmoj.dist.RealDist : The rate (frequency) at which the
	 *         SimProcesses arrive at the system.
	 */
	public desmoj.core.dist.NumericalDist<Double> getArrivalRate() {

		return _arrivalRate;
	}

	/**
	 * The <code>ArrivalProcess</code> is some kind of source for
	 * <code>SimProcess</code> es. So its lifeCycle is quite simple: Make a
	 * new <code>SimProcess</code> object and activate it. Then wait until the
	 * next <code>SimProcess</code> object is arriving at the system. Note:
	 * the first <code>SimProcess</code> object is arriving when this
	 * <code>ArrivalProcess</code> is started! This method uses the
	 * <code>createSuccessor()</code> method to create a new
	 * <code>SimProcess</code> object. If this method returns
	 * <code>null</code> the life cycle of this <code>ArrivalProcess</code>
	 * will stop.
	 */
	public void lifeCycle() throws SuspendExecution {

		// make a new SimProcess
		SimProcess arrivingProcess = createSuccessor();

		if (arrivingProcess == null) {
			setRepeating(false);
			return;
		}

		// debug out
		if (currentlySendDebugNotes()) {
			sendDebugNote("activates " + arrivingProcess.getQuotedName());
		}

		// make him arrive at the system right now (after this
		// ArrivalProcess)
		arrivingProcess.activate(new TimeSpan(0));

		// wait until next SimProcess is to arrive
		hold(new TimeSpan(_arrivalRate.sample()));

	}
}