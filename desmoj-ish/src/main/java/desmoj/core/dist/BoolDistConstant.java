package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Boolean constant "pseudo"-distribution returns a single constant predefined
 * boolean value. This "distribution" is most useful for testing purposes. The
 * value to be returned can be specified at construction time.
 * 
 * @see desmoj.core.dist.Distribution
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 * 
 */
public class BoolDistConstant extends BoolDist {

	/**
	 * Stores the status for the constant value to be returnde from this
	 * "pseudo"-distribution
	 */
	protected boolean constValue;

	/**
	 * Creates a constant boolean "pseudo" distribution with the constant value
	 * given as parameter.
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param constantValue
	 *            long : The constant <code>boolean</code> value produced by
	 *            this distribution
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public BoolDistConstant(Model owner, String name, boolean constantValue,
			boolean showInReport, boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
		constValue = constantValue;
	}

	/**
	 * Creates the default reporter for the <code>BoolDistConstant</code>
	 * distribution.
	 * 
	 * @return Reporter : The reporter for the <code>BoolDistConstant</code>
	 *         distribution
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {

		return new desmoj.core.report.BoolDistConstantReporter(this);

	}

	/**
	 * Returns the constant boolean value associated with this
	 * <code>BoolDistConstant</code> distribution
	 * 
	 * @return boolean : The constant boolean value returned by this
	 *         <code>BoolDistConstant</code> distribution
	 */
	public boolean getConstantValue() {

		return constValue;

	}

	/**
	 * Returns the next constant boolean sample of this distribution. For this
	 * "pseudo"-distribution it is always is the default value specified through
	 * the constructor or via the <em>setConstant</em> method.
	 * 
	 * @return boolean : The constant sample
	 */
	public boolean sample() {

		incrementObservations(); // increase count of samples by one

		if (this.currentlySendTraceNotes())
			this.traceLastSample(Boolean.toString(constValue));

		return constValue; // always return same constant value

	}

	/**
	 * Changes the constant value to the new one specified.
	 * 
	 * @param newValue
	 *            boolean : the new constant value to be returned by this pseudo
	 *            distribution
	 */
	public void setConstant(boolean newValue) {

		constValue = newValue; // sets constant return-value to new one given

	}
}