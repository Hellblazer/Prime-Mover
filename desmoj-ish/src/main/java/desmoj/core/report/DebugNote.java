package desmoj.core.report;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents a DebugNote produced by a simulation run whenever the modeller
 * enables the debug mode for a specific ModelComponent. Provides the basic
 * information needed to debug a model's changes of state:
 * <ul>
 * <li>The Model this DebugNote originates from</li>
 * <li>The point in simulation time that this DebugNote was issued</li>
 * <li>The name of the ModelComponent that produced the DebugNote</li>
 * <li>The textual debug information produced by that ModelComponent</li>
 * </ul>
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
public class DebugNote extends Message {

	/**
	 * The ModelComponent that produced this DebugNote.
	 */
	private String _dbgComponent;

	/**
	 * Constructs a debugnote with the given parameters.
	 * 
	 * @param origin
	 *            Model : The model that produced this debugnote
	 * @param description
	 *            java.lang.String : The actual debug information
	 * @param time
	 *            TimeInstant : The point of simulation time this debugnote was
	 *            created
	 * @param componentName
	 *            java.lang.String : The name of the modelcomponent the
	 *            debugnote evolved from
	 */
	//TODO:
	public DebugNote(Model origin, TimeInstant time, String componentName,
			String description) {

		super(origin, description, time);
		_dbgComponent = componentName;

	}

	/**
	 * Returns the name of the modelcomponent that produced this debugnote.
	 * 
	 * @return java.lang.String : The model that produced this debugnote
	 */
	public String getOrigin() {

		return _dbgComponent;

	}
}