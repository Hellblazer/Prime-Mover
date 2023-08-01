package desmoj.core.report;

/**
 * Captures all relevant information about the Regression analysis.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
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

public class RegressionReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new RegressionReporter. Note that although any
	 * Reportable is accepted you should make sure that only subtypes of
	 * Regression are passed to this constructor. Otherwise the number of column
	 * titles and their individual headings will differ from the actual content
	 * collected by this reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Regression to report about.
	 */
	public RegressionReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 11;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "xMean";
		columns[4] = "yMean";
		columns[5] = "Res.Std.Dev";
		columns[6] = "Reg.Coeff";
		columns[7] = "Intercept";
		columns[8] = "St.Dev.Reg.Coeff";
		columns[9] = "Corr.Coeff";
		columns[10] = "Unit(s)";
		groupHeading = "Regression";
		groupID = 1411; // see Reporter for more information about groupID
		entries = new String[numColumns];
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method in a way, that an array of the same length as the columntitles is
	 * produced containing the data at the point of time this method is called
	 * by someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
		if (source instanceof desmoj.core.statistic.Regression) {
			// the Regression we report about. (source = informationSource)
			desmoj.core.statistic.Regression rgr = (desmoj.core.statistic.Regression) source;

			// Title
			entries[0] = rgr.getName() + "<br>based on X: " + rgr.getXName()
					+ "<br>and Y: " + rgr.getYName();
			// (Re)set
			entries[1] = rgr.resetAt().toString();

			// Obs
			entries[2] = Long.toString(rgr.getObservations());

			// xBarMean
			// no observations made, so xBar mean can not be calculated
			if (rgr.getObservations() == 0) {
				entries[3] = "Insufficient data";
			} else // return mean value
			{
				entries[3] = Double.toString(rgr.getXMean());

				// are the x values almost constant?
				if (rgr.xIsConstant()) {
					entries[3] = entries[3] + " is constant";
				}
			}

			// yBarMean
			// no observations made, so yBar mean can not be calculated
			if (rgr.getObservations() == 0) {
				entries[4] = "Insufficient data";
			} else // return mean value
			{
				entries[4] = Double.toString(rgr.getYMean());

				// are the y values almost constant?
				if (rgr.yIsConstant()) {
					entries[4] = entries[4] + " is constant";
				}
			}

			// Res.Std.Dev
			// not enough observations made,
			// so Res.Std.Dev can not be calculated
			if (rgr.getObservations() <= 5) {
				entries[5] = "Insufficient data";
			} else // return residual std. dev.
			{
				entries[5] = Double.toString(rgr.residualStdDev());
			}

			// Reg.Coeff
			// not enough observations made,
			// so Reg.Coeff can not be calculated
			if (rgr.getObservations() <= 5) {
				entries[6] = "Insufficient data";
			} else // return reg. coeff.
			{
				entries[6] = Double.toString(rgr.regCoeff());
			}

			// Intercept
			// not enough observations made,
			// so Intercept can not be calculated
			if (rgr.getObservations() <= 5) {
				entries[7] = "Insufficient data";
			} else // return intercept
			{
				entries[7] = Double.toString(rgr.intercept());
			}

			// St.Dev.Reg.Coeff
			// not enough observations made,
			// so St.Dev.Reg.Coeff can not be calculated
			if (rgr.getObservations() <= 5) {
				entries[8] = "Insufficient data";
			} else // return St.Dev.Reg.Coeff
			{
				entries[8] = Double.toString(rgr.stdDevRegCoeff());
			}

			// Corr.Coeff
			// not enough observations made,
			// so Corr.Coeff can not be calculated
			if (rgr.getObservations() <= 5) {
				entries[9] = "Insufficient data";
			} else // return correlation coeff.
			{
				entries[9] = Double.toString(rgr.correlationCoeff());
			}
			
	        //cm 21.11.12  Extension for viewing unit
	        entries[10] = rgr.getUnitText();

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class RegressionReporter
