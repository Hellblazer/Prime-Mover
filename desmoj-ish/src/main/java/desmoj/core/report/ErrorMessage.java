package desmoj.core.report;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * An ErrorMessage is used to signal invalid situations to the modeller. An
 * ErrorMessage consists of four Strings that intend to define the kind of error
 * that occurred and describe its reason and what a modeller can do to prevent
 * it. ErrorMessages describe types of errors that can be fixed by the DESMO-J
 * or just give hints on missing or unclear parameters where certain framework
 * assumptions are made. ErrorMessages report these to the modeller. Any type of
 * error consists of the following attributes which have to be set by the
 * ErrorMessage's constructor:
 * <ul>
 * <li>error description : The operation that just failed or does not have the
 * preconditions matched</li>
 * <li>error location : The class and method that the error occurred in</li>
 * <li>error reason : The probable reason why the error occured</li>
 * <li>error prevention : A hint on how to prevent this error to occur again.
 * </li>
 * <li>error time : The point of simulation time that the error occurred.</li>
 * </ul>
 * Note that ErrorMessages relate to the modeller's use of the framework's
 * methods and correct the found problems if possible by making assumptions.
 * These corrections and their assumptions are articulated by an ErrorMessage.
 * In contrast to <code>Exceptions</code> and fatal errors, an ErrorMessage
 * does not stop the simulation run or exit the Java runtime.
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
public class ErrorMessage extends Message {

	/**
	 * The location that this message was created as a String consisting of
	 * "Classname.Methodname".
	 */
	private String _errLocation;

	/*
	 * Describes in a few sentences how the warning can be prevented in the
	 * future.
	 */
	private String _errPrevention;

	/*
	 * The reason that caused the operation to fail. It is a description of the
	 * circumstances leading to the warning. Use not more than one short
	 * sentence.
	 */
	private String _errReason;

	/**
	 * Creates an errormessage setting its parameters with the given values.
	 * 
	 * @param origin
	 *            Model : The model this errormessage evolved from
	 * @param errorDescription
	 *            java.lang.String : Description of the error
	 * @param errorLocation
	 *            java.lang.String : Class and method the error occured in
	 * @param errorReason
	 *            java.lang.String : The probable reason for the error
	 * @param errorPrevention
	 *            java.lang.String : A hint how to prevent the error
	 */
	//TODO:
	public ErrorMessage(Model origin, String errorDescription,
			String errorLocation, String errorReason, String errorPrevention,
			TimeInstant errorTime) {

		// create the Message
		super(origin, errorDescription, errorTime);

		// set the new attributes
		_errLocation = errorLocation;
		_errReason = errorReason;
		_errPrevention = errorPrevention;

	}

	/**
	 * Returns a String describing class and method that this message was sent
	 * from.
	 * 
	 * @return java.lang.String : The location this message was sent from.
	 */
	public String getLocation() {

		return _errLocation;

	}

	/**
	 * Returns a String giving a hint on how to prevent this error to happen.
	 * Note that the hint given is based on the probable reason described but
	 * must not always have that reason.
	 * 
	 * @return java.lang.String : The probable reason for this error
	 */
	public String getPrevention() {

		return _errPrevention;

	}

	/**
	 * Returns the String describing the probable reason for the error to occur.
	 * Note that the reason given here is the most probable reason for the
	 * effect to occur here. It might have its origin in other classes, so bear
	 * in mind that the casue for the error might have been created in a class
	 * not obviously related to the location this error occured.
	 * 
	 * @return java.lang.String : The probable reason for this error
	 */
	public String getReason() {

		return _errReason;

	}
}