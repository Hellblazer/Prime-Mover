package desmoj.core.report;

import desmoj.core.simulator.Reportable;
import desmoj.core.statistic.Count;

/**
 * Captures all relevant information about the Count.
 * Extended to show unit and description of reported object.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Ruth Meyer
 * @author modified by Chr. M&uuml;ller (TH Wildau) 28.11.2012
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

public class CountReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new CountReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Count are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Count to report about
	 */

	public CountReporter(Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 8;

		columns = new String[numColumns];

		columns[0] = "Title";
		columns[1] = "Type";
		columns[2] = "(Re)set";
		columns[3] = "Obs";
		columns[4] = "Current Value";
		columns[5] = "Min";
		columns[6] = "Max";
        columns[7] = "Unit";
		groupHeading = "Counts and Aggregates";

		groupID = 1311; // see Reporter for more information about groupID

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
		if (source instanceof Count) {
			// the Count we report about
			Count cnt = (Count) source;
			// source = informationSource
			// Title
			entries[0] = cnt.getName();
            // Type
            entries[1] = "Count";
            // (Re)set
			entries[2] = cnt.resetAt().toString();
			// Observations
			entries[3] = Long.toString(cnt.getObservations());
			// current value
			entries[4] = Long.toString(cnt.getValue());
			// Min
			entries[5] = Long.toString(cnt.getMinimum());
			// Max
			entries[6] = Long.toString(cnt.getMaximum());
			//cm 21.11.12  Extension for viewing unit
            entries[7] = cnt.getUnitText();

		}

		else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class CountReporter
