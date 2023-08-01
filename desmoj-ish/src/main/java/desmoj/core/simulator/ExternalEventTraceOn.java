package desmoj.core.simulator;

/**
 * external event switching on the trace output for the experiment. The
 * messagemanager's channel for tracenotes is switched on to forward tracenotes
 * to the configured Output.
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
public class ExternalEventTraceOn extends ExternalEvent {
	/**
	 * Creates the external event to start the trace output for an experiment.
	 * 
	 * @param owner
	 *            desmoj.Model : The model this external event is associated to
	 * @param showInTrace
	 *            boolean : The flag indicating if this external event is shown
	 *            in the trace output
	 */
	public ExternalEventTraceOn(Model owner, boolean showInTrace) {

		super(owner, "TraceOn", showInTrace);

	}

	/**
	 * Switches the messagemanager's trace note channel on to forward tracenotes
	 * to the configured output.
	 */
	public void eventRoutine() {

		Experiment ex = getModel().getExperiment();
		ex.getMessageManager().switchOn(Experiment.tracenote);
		if (currentlySendTraceNotes())
			sendTraceNote("Trace switched on");

	}
}