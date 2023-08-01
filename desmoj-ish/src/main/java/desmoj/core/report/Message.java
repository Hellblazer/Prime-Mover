package desmoj.core.report;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Base class for all message types used in the DESMO-J framework. Provides the
 * basic functionality and information common to all messages. Other Messages
 * must be derived from this class and extend the attributes to transport the
 * desired information. This allows for adding custom message bearing specific
 * information. Given the references to the objects to be displayed in a
 * message, the constructor extracts their names if possible. In case of
 * <code>null</code> references given as parameters, the String "----" is taken
 * instead.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public class Message {

	private String _expName;

	/**
	 * The name of the model that produced this message.
	 */
	private String _modName;

	/**
	 * The textual message to be published by a messagereceiver.
	 */
	private String _msgDescription;

	/**
	 * The point of simulation time that this message was created.
	 */
	private desmoj.core.simulator.TimeInstant _msgTime;

	/**
	 * Constructs a message with the given parameters.
	 * 
	 * @param origin
	 *            Model : The model that produced this message
	 * @param description
	 *            java.lang.String : The actual message text
	 * @param time
	 *            TimeInstant : The point in simulation time this message was
	 *            created
	 */
	// TODO:
	public Message(Model origin, String description, TimeInstant time) {

		// fill in the attributes, checking vor void parameters
		// 
		// @modified by Nick Denz on 18.9.07
		// Reason: Some clients (e.g. SimTime) use this constructor
		// (indirectly) without passing an origin model. In this case,
		// querying the experiment name also fails. I have therefore
		// added an empty experiment name similar to the empty model name.
		if (origin == null) {
			_modName = "----";
			_expName = "----";
		} else {
			_modName = origin.getName();
			_expName = origin.getExperiment().getName();
		}

		if (description == null)
			_msgDescription = "----";
		else
			_msgDescription = description;

		// save the simulation time the message was created at
		_msgTime = time;

	}

	/**
	 * Returns the textual description of this message as a String.
	 * 
	 * @return java.lang.String : The message's description
	 */
	public String getDescription() {

		return _msgDescription;

	}

	public String getExperimentName() {
		return _expName;
	}

	/**
	 * Returns the name of the model that produced this message.
	 * 
	 * @return java.lang.String : The the name of the model that produced this
	 *         message
	 */
	public String getModelName() {

		return _modName;

	}

	/**
	 * Returns the time the message was (created) sent as a TimeInstant object.
	 * 
	 * @return desmoj.core.simulator.TimeInstant : The time the message was sent
	 *         as a TimeInstant object.
	 */
	public TimeInstant getSendTime() {

		return _msgTime;
	}

	/**
	 * Returns the point of simulation time that this message was created as a
	 * String.
	 * 
	 * @return java.lang.String : the point of simulation time this message was
	 *         created
	 */
	public String getTime() {

		if (_msgTime == null)
			return "----";
		else
			return _msgTime.toString();

	}

	/**
	 * Overrides the Object's <code>toString()</code> method to return the
	 * message's description when this object is used as a String.
	 * 
	 * @return java.lang.String : The message's description
	 */
	public String toString() {

		return _msgDescription;

	}
}