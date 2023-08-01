package desmoj.core.exception;

import desmoj.core.report.ErrorMessage;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Exception is thrown to indicate that the simulation has finished properly. It
 * is needed to unlock all threads still alive after the simulation has reached
 * its criterium to stop. Users must not make use of it nor is this exception
 * supposed to be caught by users. Its use inside the DESMOJ framework is
 * transparent because it is derived from <code>RuntimeException</code> which
 * isthe root for a tree of exceptions that are automatically rethrown by any
 * Java method thus not needing to be rethrown by the user, too.
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
public class SimFinishedException extends DESMOJException {
    private static final long serialVersionUID = 1L;

    /**
	 * Constructs a SimFinishedException inserting the given position String and
	 * the given simulation time in the ErrorMessage documenting this exception.
	 * 
	 * @param position
	 *            java.lang.String : contains the position, i.e. Object and
	 *            method that throws this exception
	 * @param time
	 *            TimeInstant : The point in simulation time that this exception is
	 *            thrown
	 */
	public SimFinishedException(Model origin, String position, TimeInstant time) {

		super(new ErrorMessage(origin, "SimFinishedException thrown!",
				"Position " + position, "Simulation has come to an end.",
				"No Error.", time));

	}
}