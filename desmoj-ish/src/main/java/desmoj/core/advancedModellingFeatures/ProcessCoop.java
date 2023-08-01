package desmoj.core.advancedModellingFeatures;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Schedulable;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * ProcessCoop is the object representing the process cooperation between two
 * processes. It is intended that this process cooperation is used with the
 * <code>WaitQueue</code> construct, where one process is the master which
 * really executes the cooperation and the other process is the slave waiting
 * for a master to lead him through the cooperation. That means that during the
 * cooperation the master is active and the slave is passive. The action carried
 * out for the two processes together is described in the virtual method
 * <code>cooperation</code>, which is to be implemented by the user building the
 * model. This class is encapsulating the cooperation of (at least) these two
 * processes. When using the WaitQueue construct the master will be activated
 * after the cooperation is done and the slave will be activated after the
 * master (if it has not been activated during the cooperation already).
 * 
 * @see desmoj.core.advancedModellingFeatures.WaitQueue
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
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

public abstract class ProcessCoop<M extends SimProcess, S extends SimProcess> extends desmoj.core.simulator.ModelComponent {

	// ****** methods ******

	/**
	 * Constructor for a ProcessCoop where a master and a slave process are
	 * cooperating.
	 * 
	 * @param owner
	 *            Model : The model this ProcessCoop is associated to.
	 * @param name
	 *            java.lang.String : The ProcessCoop's name
	 * @param showInTrace
	 *            boolean : Flag for trace to produce trace messages.
	 */
	public ProcessCoop(desmoj.core.simulator.Model owner, String name,
			boolean showInTrace) {
		super(owner, name, showInTrace); // construct ModelComponent
	}

	/**
	 * Activates (schedules) the current SimProcess at the current Simulation
	 * time plus the offset <code>dt</code>. This method is passed through to
	 * the currently running master process.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the time now, when the current
	 *            process is to be activated.
	 */
	protected void activate(TimeSpan dt) throws SuspendExecution {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: activate()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.activate(dt);
	}

	/**
	 * Schedules the current SimProcess to be activated directly after the given
	 * Schedulable, which itself is already scheduled. Please make sure that the
	 * Schedulable given as parameter is actually scheduled. This method is
	 * passed through to the currently running master process.
	 * 
	 * @param after
	 *            Schedulable : The Schedulable the currently running process
	 *            should be scheduled after.
	 */
	protected void activateAfter(Schedulable after) {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: activateAfter()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.activateAfter(after);
	}

	/**
	 * Schedules the current SimProcess to be activated directly before the
	 * given Schedulable, which itself is already scheduled. Please make sure
	 * that the Schedulable given as parameter is actually scheduled. This
	 * method is passed through to the currently running master process.
	 * 
	 * @param before
	 *            Schedulable : The Schedulable the currently running process
	 *            should be scheduled before.
	 */
	protected void activateBefore(Schedulable before) {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: activateBefore()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.activateBefore(before);
	}

	/**
	 * The user building the model has to implement this method in a subclass.
	 * Here the action of the cooperation carried out by the master process will
	 * be described. If simulation time is used to perform some action the user
	 * has to model this by calling the method <code>hold(TimeSpan dt)</code>.
	 * The user (the one who is building the model) is responsible to implement
	 * this correctly in this <code>cooperation()</code> method.
	 * 
	 * @param master
	 *            SimProcess : The master process which really carries out the
	 *            cooperation.
	 * @param slave
	 *            SimProcess : The slave process which is lead through the
	 *            cooperation by the master.
	 * @throws SuspendExecution Marker exception for Quasar.
	 */
	protected abstract void cooperation(M master, S slave) throws SuspendExecution;

	// the user has to implement the cooperation action here...

	/**
	 * Returns the priority of the current SimProcess (usually the master
	 * process). Default priority is zero. Higher priorities are positive, lower
	 * priorities negative. The priority determines the position in a waiting
	 * queue. This method is passed through to the currently running master
	 * process.
	 * 
	 * @return int : The priority of the SimProcess.
	 */
	protected int getPriority() {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "Zero is returned as priority!", "ProcessCoop: "
					+ getName() + " Method: getPriority()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return 0;
		}

		return currentProcess.getQueueingPriority();
	}

	/**
	 * Holds the current SimProcess for the given time dt. Hold is used to
	 * simulate a time period during which the process is working on something
	 * in reality. But in the simulation the process is not active. Only the
	 * result is important for the simulation. (So the simulation time will be
	 * set to the new value and all the attributes which have changed during
	 * this time period are changed.) This method is passed through to the
	 * currently running master process.
	 * 
	 * @param dt
	 *            desmoj.TimeSpan : The current SimProcess will be passivated
	 *            during this time period.
	 */
	protected void hold(TimeSpan dt) throws SuspendExecution {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: hold()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.hold(dt);
	}

	/**
	 * Passivates the current SimProcess for an unknown time period. It can only
	 * be activated by other objects of the simulation (SimProcesses or
	 * Entities). This method is passed through to the currently running master
	 * process.
	 */
	protected void passivate() throws SuspendExecution {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: passivate()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.passivate();
	}

	/**
	 * Reactivates (reschedules) the current SimProcess at the current point of
	 * simulation time plus the offset dt. This method is passed through to the
	 * currently running master process.
	 * 
	 * @param dt
	 *            TimeSpan : The offset to the time now, when the current
	 *            process is to be reactivated.
	 */
	protected void reActivate(TimeSpan dt) throws SuspendExecution {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: reActivate()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.reActivate(dt);
	}

	/**
	 * Sets the queueing priority of the current SimProcess to a new integer value. Zero
	 * is the default priority. Negative priorities are lower, positive
	 * priorities are higher. All values should be inside the range defined by
	 * Java's integral <code>integer</code> data type. The priority determines
	 * the position in a waiting-queue. This method is passed through to the
	 * currently running master process.
	 * 
	 * @param newPriority
	 *            int : The new priority value.
	 */
	protected void setPriority(int newPriority) {
		// get the current process
		SimProcess currentProcess = currentSimProcess();

		if (currentProcess == null) {
			sendWarning("The current process of a cooperation is not found. "
					+ "The attempted action is ignored!", "ProcessCoop: "
					+ getName() + " Method: setPriority()",
					"The current process is only a null pointer.",
					"Make sure that only real SimProcesses are cooperating.");
			return;
		}

		currentProcess.setQueueingPriority(newPriority);
	}
} // end abstract class ProcessCoop
