package desmoj.core.simulator;

/**
 * external event switching off the trace output for the experiment. The
 * messagemanager's channel for tracenotes is switched off to stop forwarding
 * tracenotes to the configured output.
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
public class ExternalEventTraceOff extends ExternalEvent {
	/**
	 * Creates the external event to stop the trace output for an experiment.
	 * 
	 * @param owner
	 *            desmoj.Model : The model this external event is associated to
	 * @param showInTrace
	 *            boolean : The flag indicating if this external event is shown
	 *            in the trace output
	 */
	public ExternalEventTraceOff(Model owner, boolean showInTrace) {

		super(owner, "TraceOff", showInTrace);

	}

	/**
	 * Switches the messagemanager's trace note channel off to stop forward
	 * tracenotes to the configured output.
	 */
	public void eventRoutine() {

		Experiment ex = getModel().getExperiment();
		if (currentlySendTraceNotes())
			sendTraceNote("Trace switched off");
		ex.getMessageManager().switchOff(Experiment.tracenote);

	}
}