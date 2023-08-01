package desmoj.core.exception;

import desmoj.core.report.ErrorMessage;

/**
 * General exception for the desmoj simulation framework. All specific
 * exceptions extend this class, thus building a hierarchic tree of possible
 * exceptions being thrown by the framework and keeping all simulation related
 * exceptions close together. Most exceptions are thrown when the continuation
 * of the simulation can not be determined. This is especially the case with
 * <code>null</code> references passed to a method expecting valid parameters.
 * Note that these exceptions must not be caught by the user and that the user
 * does not need to re-throw these exceptions, since they all inherit from class
 * <code>java.exception.RuntimeException</code> which has the special feature
 * to be re-thrown automatically by any method.
 * 
 * @see java.lang.RuntimeException
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim lechler
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
public class DESMOJException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	/**
	 * Contains the error message containing a description of the circumstances
	 * leading to this exception being thrown.
	 */
	private ErrorMessage _errMsg;

	/**
	 * Constructs a standard DESMO-J exception with an error message needed to
	 * properly describe what error occurred, where it occurred, what reasons
	 * could be responsible for it and what could be done to prevent this error
	 * from occurring.
	 * 
	 * @param message
	 *            desmoj.report.ErrorMessage : Describes the error leading to
	 *            this exception
	 */
	public DESMOJException(ErrorMessage message) {

		// construct a simple RuntimeException with the description and probable
		// error reason as text.
		super("DESMOJException\n"
		        + "Description: " + message.getDescription() + "\n" 
		        + "Location:    " + message.getLocation() + "\n" 
		        + "Reason:      " + message.getReason() + "\n" 
		        + "Prevention:  " + message.getPrevention());
		_errMsg = message;

	}

    /**
     * Constructs a standard DESMO-J exception with an error message needed to
     * properly describe what error occurred, where it occurred, what reasons
     * could be responsible for it and what could be done to prevent this error
     * from occurring. The exception that caused this one is specified, too.
     * 
     * @param message
     *            desmoj.report.ErrorMessage : Describes the error leading to
     *            this exception
     * @param cause
     *            The exception describing the underlying cause.
     */
    public DESMOJException(ErrorMessage message, Throwable cause) {

        // construct a simple RuntimeException with the description and probable
        // error reason as text.
        super("DESMOJException\n"
                + "Description: " + message.getDescription() + "\n" 
                + "Location:    " + message.getLocation() + "\n" 
                + "Reason:      " + message.getReason() + "\n" 
                + "Prevention:  " + message.getPrevention(),
            cause);
        _errMsg = message;

    }
    
	/**
	 * Returns the error message describing the circumstances leading to this
	 * exception being thrown. Use this method to extract the message and pass
	 * it to the Experiment's MessageDistributor to write it to the ErrorFile.
	 * 
	 * @return desmoj.report.ErrorMessage : The errormessage describing the
	 *         reasons for this Exception
	 */
	public ErrorMessage getErrorMessage() {

		return _errMsg;

	}
}