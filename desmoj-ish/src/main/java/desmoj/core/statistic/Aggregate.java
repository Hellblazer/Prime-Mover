package desmoj.core.statistic;

import java.util.Observable;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * The <code>Aggregate</code> class is simply counting (aggregating) a continuous (i.e. double)
 * value during an experiment. Be careful the aggregate can be updated with positive and negative
 * values!
 * 
 * To get this <code>Aggregate</code> object updated automatically every time a
 * <code>ValueSupplier</code> has changed, call the <code>addObserver
 * (Observer)</code>-method from the <code>ValueSupplier</code> of interest, 
 * where Observer is this <code>Aggregate</code> object. <br>
 * This must be done by the user in his model! <br>
 * Consider usage of class <code>Count</code> to aggregate integer values. 
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Ruth Meyer, Johannes G&ouml;bel
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

public class Aggregate extends desmoj.core.statistic.StatisticObjectSupportingTimeSpans {

	// ****** attributes ******

	/**
	 * Flag indicating if this aggregate will retain its value during a reset.
	 * Default is false.
	 */
	private boolean _isResetResistant;

	/**
	 * The maximum of all values so far
	 */
	private double _max;

	/**
	 * The minimum of all values so far
	 */
	private double _min;

	/** 
	 * The current value of the aggregate
	 */
	private double _value;

	// ****** methods ******

	/**
	 * Constructor for a Aggregate object.
	 * 
	 * @param ownerModel
	 *            Model : The model this Aggregate is associated to
	 * @param name
	 *            java.lang.String : The name of this Aggregate object
	 * @param showInReport
	 *            boolean : Flag for showing the report Set it to
	 *            <code>true</code> if this Aggregate should show up in report.
	 *            Set it to <code>false</code> if this Aggregate should not be
	 *            shown in report.
	 * @param showInTrace
	 *            boolean : Flag for showing this Aggregate in trace files. Set it
	 *            to <code>true</code> if this Aggregate should show up in trace.
	 *            Set it to <code>false</code> if this Aggregate should not be
	 *            shown in trace.
	 */
	public Aggregate(Model ownerModel, String name, boolean showInReport,
			boolean showInTrace) {
		this(ownerModel, name, showInReport, showInTrace, false);
	}

	/**
	 * Constructor for a Aggregate object.
	 * 
	 * @param ownerModel
	 *            Model : The model this Aggregate is associated to
	 * @param name
	 *            java.lang.String : The name of this Aggregate object
	 * @param showInReport
	 *            boolean : Flag for showing the report Set it to
	 *            <code>true</code> if this Aggregate should show up in report.
	 *            Set it to <code>false</code> if this Aggregate should not be
	 *            shown in report.
	 * @param showInTrace
	 *            boolean : Flag for showing this Aggregate in trace files. Set it
	 *            to <code>true</code> if this Aggregate should show up in trace.
	 *            Set it to <code>false</code> if this Aggregate should not be
	 *            shown in trace.
	 * @param isResetResistant
	 *            boolean : Flag for retaining the value of the aggregate during resets.
	 *            Set it to <code>true</code> if this Aggregate should retain its
	 *            current value and only reset min, max and observations to 0.
	 *            Set it to <code>false</code> if this Aggregate should also reset
	 *            its value to 0.
	 */
	public Aggregate(Model ownerModel, String name, boolean showInReport,
			boolean showInTrace, boolean isResetResistant) {
		super(ownerModel, name, showInReport, showInTrace);

		this._min = this._max = 0; // no minimum or maximum so far

		this._value = 0; // nothing aggregated so far
		this._isResetResistant = isResetResistant; // set resistance flag
	}

	/**
	 * Returns a Reporter to produce a report about this Aggregate.
	 * 
	 * @return desmoj.report.Reporter : The Reporter for this Aggregate.
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {
		return new desmoj.core.report.AggregateReporter(this);
	}

	/**
	 * Returns the maximum value observed so far.
	 * 
	 * @return double : The maximum value observed so far.
	 */
	public double getMaximum() {
		return this._max;
	}

	/**
	 * Returns the minimum value observed so far.
	 * 
	 * @return double : The minimum value observed so far.
	 */
	public double getMinimum() {
		return this._min;
	}

	/**
	 * Returns the current value of the aggregate.
	 * 
	 * @return double : the current value of the aggregate.
	 */
	public double getValue() {
		return this._value;
	}
	
    /**
	 * Resets this Aggregate object by resetting (nearly) all variables to zero. If
	 * the flag <code>isResetResistant</code> is set to <code>true</code>
	 * the value of the aggregate will NOT be changed.
	 */
	public void reset() {
		super.reset(); // reset the Reportable, too.

		// really reset the value of the aggregate?
		if (!this._isResetResistant) {
		    this._min = this._max = 0;
			this._value = 0;
		}
	}

	/**
	 * Increments the value of this <code>Aggregate</code> object by the value
	 * given in the parameter n.
	 * 
	 * @param n
	 *            double : The number that will be added to the value of this
	 *            <code>Aggregate</code> object.
	 */
	public void update(double n) {
		incrementObservations(1); // use the method from the Reportable class

		this._value += n; // update current value

		if (this._value < _min) {
			_min = this._value; // update min
		}

		if (this._value > _max) {
			_max = this._value; // update max
		}

		traceUpdate(); // leave a message in the trace
	}

	/**
	 * Implementation of the virtual <code>update(Observable, Object)</code>
	 * method of the <code>Observer</code> interface. This method will be
	 * called automatically from an <code>Observable</code> object within its
	 * <code>notifyObservers()</code> method. <br>
	 * If no Object (a<code>null</code> value) is passed as arg, the actual
	 * value of the ValueSupplier will be fetched with the <code>value()</code>
	 * method of the ValueSupplier. Otherwise it is expected that the actual
	 * value is passed in the Object arg.
	 * 
	 * @param o
	 *            java.util.Observable : The Observable calling this method
	 *            within its own <code>notifyObservers()</code> method.
	 * @param arg
	 *            Object : The Object with which this <code>Aggregate</code> is
	 *            updated. Normally a double number which is added to the value of the aggregate
	 *            or <code>null</code>.
	 */
	public void update(Observable o, Object arg) {
		if (o == null) // null was passed instead of an Observable
		{
			sendWarning(
					"Attempt to update a Aggregate with no reference to an "
							+ "Observable. The attempted action will be ignored!",
					"Aggregate: " + this.getName()
							+ " Method: update (Observable o, " + "Object arg)",
					"The passed Observable: o in this method is only a null pointer.",
					"The update()-method was not called via notifyObservers() from an "
							+ "Observable. Who was calling it? Why don't you let the Observable do"
							+ " the work?");

			return; // do nothing, just return
		}

		if (arg == null) // update was called with no arg Object
		{
			// get the actual value from the ValueSupplier (= Observable)
			double n = ((ValueSupplier) o).value();
			update(n);

		} else {
			if (arg instanceof Number) {
				// get the value out of the Object arg
				double n = convertToDouble(arg);
				update(n);

			} else {
				sendWarning(
						"Attempt to update a Aggregate with an object, that can "
								+ "not be recognized. The attempted action will be ignored!",
						"Aggregate: " + this.getName()
								+ " Method: update (Observable o, "
								+ "Object arg)",
						"The passed Object: arg in this method is not a Number.",
						"Make sure to pass as Object: arg in the update()-method only "
								+ "objects of the class: java.lang.Number or null!");

				return; // do nothing, just return
			}
		}

	}

	@Override
    /**
     * {@inheritDoc}
     */
    public void update(TimeSpan t) {
        this.setShowTimeSpansInReport(true);
        this.update(t.getTimeAsDouble());
    }
} // end class
