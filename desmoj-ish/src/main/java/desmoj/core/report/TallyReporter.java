package desmoj.core.report;

import desmoj.core.simulator.TimeSpan;

/**
 * Captures all relevant information about the Tally.
 * Extended to show unit and description of reported object.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
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
public class TallyReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new TallyReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Tally are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Tally to report about.
	 */
	public TallyReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter
		
		numColumns = 8;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "Mean";
		columns[4] = "Std.Dv";
		columns[5] = "Min";
		columns[6] = "Max";
		columns[7] = "Unit";
		groupHeading = "Tallies";
		groupID = 1611; // see Reporter for more information about groupID
		entries = new String[numColumns];
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method in a way, that an array of the same length as the column titles is
	 * produced containing the data at the point of time this method is called
	 * by someone else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {
		if (source instanceof desmoj.core.statistic.Tally) {
			// the Tally we report about (source = informationSource)
			desmoj.core.statistic.Tally tl = (desmoj.core.statistic.Tally) source;
			boolean _showTimeSpansInReport     = tl.getShowTimeSpansInReport();
			desmoj.core.statistic.TallyRunning tlr = null;
			if (tl instanceof desmoj.core.statistic.TallyRunning) {
				tlr = (desmoj.core.statistic.TallyRunning) tl;
			}

			// Title
			entries[0] = tl.getName();
			// (Re)set
			entries[1] = tl.resetAt().toString();
			// Obs
			entries[2] = Long.toString(tl.getObservations());
			// Mean
			// no observations made, so Mean can not be calculated
			if (tl.getObservations() == 0) {
				entries[3] = "Insufficient data";
			} else // return mean value
			{
				entries[3] = this.format(_showTimeSpansInReport, tl.getMean());
				if (tlr != null)
					entries[3] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMeanLastN())
							+ ")";
			}

			// Std.Dev
			// not enough observations are made, so Std.Dev can not be
			// calculated
			if (tl.getObservations() < 2) {
				entries[4] = "Insufficient data";
			} else // return standard deviation
			{
				entries[4] = this.format(_showTimeSpansInReport, tl.getStdDev());
				if (tlr != null)
					entries[4] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getStdDevLastN())
							+ ")";
			}

			// Min
			if (tl.getObservations() == 0) {
				entries[5] = "Insufficient data";
			} else {
				entries[5] = this.format(_showTimeSpansInReport, tl.getMinimum());
				if (tlr != null)
					entries[5] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMinimumLastN())
							+ ")";
			}

			// Max
			if (tl.getObservations() == 0) {
				entries[6] = "Insufficient data";
			} else {
				entries[6] = this.format(_showTimeSpansInReport, tl.getMaximum());
				if (tlr != null)
					entries[6] += " (last "
							+ tlr.getSampleSizeN()
							+ " obs: "
							+ this.format(_showTimeSpansInReport, tlr.getMaximumLastN())
							+ ")";
			}
			
			//cm 21.11.12  Extension for viewing unit
            entries[7] = tl.getUnitText();

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
	
	private String format(boolean showTimeSpans, double value){
		String out = Double.toString(value);
		if(showTimeSpans && value < 0.0) 					out += " (Invalid)";
		else if(showTimeSpans && value >= Long.MAX_VALUE) 	out += " (Invalid)";
		else if(showTimeSpans) 								out = new TimeSpan(value).toString();
		return out;
	}

} // end class TallyReporter
