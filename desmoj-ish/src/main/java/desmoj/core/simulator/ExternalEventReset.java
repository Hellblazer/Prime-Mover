package desmoj.core.simulator;

/**
 * An external event to reset the statistic counters of the model.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Kristof Hamann
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
 */
public class ExternalEventReset extends ExternalEvent {

	/**
	 * Creates an external event to reset the statistic counters of the model.
	 * @param owner The external event's model
	 * @param showInTrace Flag for showing this external event in tracemessages
	 */
	public ExternalEventReset(Model owner, boolean showInTrace) {
		super(owner, "Reset", showInTrace);
	}

	/**
	 * The event routine resets the statistic counters of the model.
	 * @see Model#reset()
	 */
	public void eventRoutine() {
		getModel().reset();
	}
}
