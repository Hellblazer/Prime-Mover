package desmoj.core.simulator;

/**
 * external event switching off the debug output for the experiment. The
 * messagemanager's channel for debugnotes is switched off to stop forwarding
 * debugnotes to the configured Output.
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
public class ExternalEventDebugOff extends ExternalEvent {
	/**
	 * Creates the external event to stop the debug output for an experiment.
	 * 
	 * @param owner
	 *            desmoj.Model : The model this external event is associated to
	 * @param showInTrace
	 *            boolean : The flag indicating if this external event is shown
	 *            in the trace output
	 */
	public ExternalEventDebugOff(Model owner, boolean showInTrace) {

		super(owner, "DebugOff", showInTrace);

	}

	/**
	 * Switches the messagemanager's debug note channel off to stop forwarding
	 * debugnotes to the configured output.
	 */
	public void eventRoutine() {

		Experiment ex = getModel().getExperiment();
		if (currentlySendTraceNotes())
			sendTraceNote("Debug switched off");
		ex.getMessageManager().switchOff(Experiment.debugnote);

	}

}