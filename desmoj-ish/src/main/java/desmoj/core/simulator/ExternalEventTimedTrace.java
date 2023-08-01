package desmoj.core.simulator;

/**
 * The ExternalEventTimedTrace class allows for inserting items with true time
 * values into the trace file. When the event routine is called an appropriate
 * trace note is sent.
 * <p>
 * The <code>Experiment</code> class uses <code>ExternalEventTimedTrace</code>
 * events to write the start and stop time of the trace period as true time into
 * the trace file.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Ruth Meyer
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
public class ExternalEventTimedTrace extends ExternalEvent {

	/**
	 * The true time corresponding to the simulation time of this event.
	 */
	String trueTime;

	/**
	 * A description of the true time / sim time value.
	 */
	String what;

	/**
	 * Constructs an ExternalEventTimedTrace with the given true time, description 
	 * and model.
	 * 
	 * @param trueTime
	 *            String : the true time value to be written into the trace file
	 * @param what
	 *            String : a short description of the true time value
	 * @param owner
	 *            Model : the model this event shall belong to
	 */
	public ExternalEventTimedTrace(String trueTime, int refUnit, String what,
			Model owner) {
		super(owner, "TimedTrace", true);
		this.trueTime = trueTime;
		this.what = what;
	}

	/**
	 * Sends a trace note with the description and the true time including the
	 * time unit.
	 */
	public void eventRoutine() {
		// Experiment ex = getModel().getExperiment();
		// ex.getMessageManager().switchOn(ex.tracenote);
		if (currentlySendTraceNotes())
			sendTraceNote(what + ": " + trueTime);
	}
}