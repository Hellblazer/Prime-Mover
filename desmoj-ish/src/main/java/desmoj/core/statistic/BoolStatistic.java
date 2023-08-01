package desmoj.core.statistic;

import desmoj.core.simulator.Model;

/**
 * The <code>BoolStatistic</code> class is providing a statistic analysis about 
 * a boolean value. Statistics include <code>true</code>-ratio and total number of observations.
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

public class BoolStatistic extends desmoj.core.simulator.Reportable {

	// ****** attributes ******

	/**
	 * Number of <code>true</code>s observed
	 */
	private long _freq_true;

	/**
	 * Constructor for a BoolStatistic object. No connection to a
	 * <code>ValueSupplier</code>.
	 * 
	 * @param ownerModel
	 *            Model : The model this BoolStatistic is associated to
	 * @param name
	 *            java.lang.String : The name of this BoolStatistic object
	 * @param showInReport
	 *            boolean : Flag for showing the report about BoolStatistic Tally.
	 * @param showInTrace
	 *            boolean : Flag for showing the trace output of BoolStatistic Tally.
	 */
	public BoolStatistic(Model ownerModel, String name, boolean showInReport,
			boolean showInTrace) {
		// call the constructor of ValueStatistics
		super(ownerModel, name, showInReport, showInTrace);
		this._freq_true = 0;
	}
	
    // ****** methods ******

	/**
	 * Returns a Reporter to produce a report about this BoolStatistic.
	 * 
	 * @return desmoj.report.Reporter : The Reporter for this BoolStatistic.
	 */
	public desmoj.core.report.Reporter createDefaultReporter() {
		return new desmoj.core.report.BoolStatisticReporter(this);
	}

	/**
     * Returns the number of <code>true</code>s observed so far.
     * 
     * @return long : The number of <code>true</code>s observed so far.
     */
	public long getTrueObs() {
	    return this._freq_true;	    
	}


	/**
	 * Returns the ratio of <code>true</code>s per total observations so far.
	 * 
	 * @return double : The ratio of <code>true</code>s per total observations so far.
	 */
	public double getTrueRatio() {
		if (getObservations() == 0) {
			sendWarning(
					"Attempt to get ratio of trues per total observations, but there is not "
							+ "sufficient data yet. UNDEFINED (-1.0) will be returned!",
					"BoolStatistic: " + this.getName() + " Method: double getTrueRatio()",
					"You can not calculate a ratio of trues as long as no data is collected.",
					"Make sure to ask for ratio of trues only after some data has been "
							+ "collected already.");

			return StatisticObject.UNDEFINED; // return UNDEFINED = -1.0
		}

		// calculate the mean value
		double ratio = 1.0 * this.getTrueObs() / getObservations();
		// return the rounded mean value
		return desmoj.core.statistic.ValueStatistics.round(ratio);
	}

	/**
	 * Resets this <code>BoolStatistic</code> object by deleting a observations this far.
	 */
	public void reset() {
		super.reset(); // reset the StatisticObject, too.
		this._freq_true = 0;
	}

	/**
	 * Updates this <code>BoolStatistic</code> object with a boolean double value given as
	 * parameter.
	 * 
	 * @param val
	 *            boolean : The value with which this <code>BoolStatistic</code> will
	 *            be updated.
	 */
	public void update(boolean val) {
	    
	    if (val == true) this._freq_true++;
	    this.incrementObservations();
	}
} // end class BoolStatistic