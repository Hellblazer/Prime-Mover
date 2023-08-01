package desmoj.core.statistic;

/**
 * Supplies observers with values of their interest. It resembles the
 * "observable" part of the observer pattern as described in [Gamm95]. Thus it
 * extends the standard "Observable" class provided by the Java.util package.
 * Usually, statistical data collectors are interested in the new value whenever
 * a certain status variable of a simulation object changes. Also there are
 * statistical evaluation systems, such as AKAROA, which extract information
 * from several running simulations on different remote computers to gain better
 * statistical data and finally, extracting values during runtime enables better
 * visualizatioin of the simulation progress if these values are displayed using
 * a graphical interface. There are two possible ways to have other objects
 * register changes of another object's status:
 * <ol>
 * <li>Call the <code>"update(double value)"</code> method of an observer to
 * pass the changed value directly to one other object interested. Using the
 * direct call allows easy passing of a value to a single observer. No instance
 * of this <code>ValueSupplier</code> class is needed using direct passing of
 * values.</li>
 * <li>Using the <code>ValueSupplier</code> to publish the information to
 * interested observers requires you to derive a class and implement the call of
 * the observer's <code>update(Observable, Object)</code> method with the
 * required values. This way of passing values involves more code to be written,
 * but offer the potential to have multiple observers and change the number of
 * observers during runtime. These options are not available when using the
 * simpler version above.</li>
 * </ol>
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * @author modifications as the method:
 *         <code>notifyStatistics(Object arg)</code> by Soenke Claassen
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
public abstract class ValueSupplier extends java.util.Observable {

	/*
	 * The name of the valuesupplier as a string.
	 */
	private String _myName;

	/**
	 * Constructs a simple valuesupplier by giving it a name.
	 */
	public ValueSupplier(String name) {
		// call the constructor of java.util.Observable
		super();

		// get hold of the name of this ValueSupplier
		this._myName = name;
	}

	/**
	 * Returns the name of the named object. This is the same name displayed in
	 * reports and trace files when this named object is shown in those reports
	 * or trace files.
	 * 
	 * @return java.lang.String : The name of the named object
	 */
	public String getName() {

		return _myName;

	}

	/**
	 * Returns the quoted name of the named object. This is the name displayed
	 * in reports and trace files when this named object is shown.
	 * 
	 * @return java.lang.String : The quoted name of the named object
	 */
	public String getQuotedName() {

		return "'" + _myName + "'";

	}

	/**
	 * Indicates that this ValueSupplier (as an Observable) has changed and
	 * notifies all its observers of this change providing the modified value as
	 * parameter. This is the "Push-model", thus pushing the value (in the
	 * parameter <code>arg</code>) to the Observer (thus the
	 * <code>StatisticObject</code>).
	 * 
	 * @param arg
	 *            Object : The Object wrapping the value with which the
	 *            observers will be updated
	 */
	public void notifyStatistics(Object arg) {
		setChanged(); // call the method from the Observable

		notifyObservers(arg); // call the method from the Observable
	}

	/**
	 * Changes the name of the named object. This might be necessary for
	 * automatically created named object of a user defined model, but should
	 * not be used for elements of the framework or basic objects of the user
	 * model. Changing names of objects while runtime will confuse any trace
	 * output and generally make traces more difficult if not impossible to
	 * follow.
	 * 
	 * @param newName
	 *            java.lang.String : The new name for the named object
	 */
	public void rename(String newName) {

		_myName = newName;

	}

	/**
	 * Overrides the java.lang.Object's toString method to return the named
	 * object's name when given as parameter to a method that expects a string
	 * to be passed.
	 * 
	 * @return java.lang.String : The named object's name
	 */
	public String toString() {
		return _myName;
	}

	/**
	 * When using the Pull-Model or the automatic update function this method
	 * must be overridden by the user in a way that it provides the value (as a
	 * <code>double</code>) desired.
	 * 
	 * @return double : The value calculated within this method.
	 */
	public abstract double value();
} // end class
