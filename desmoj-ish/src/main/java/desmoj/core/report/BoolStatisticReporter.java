package desmoj.core.report;

/**
 * Captures all relevant information about the BoolStatistic.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
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
public class BoolStatisticReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new BoolStatisticReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of BoolStatistic are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The BoolStatistic to report about.
	 */
	public BoolStatisticReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 5;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "ObsTrue";
		columns[4] = "Ratio";
        groupHeading = "Boolean Statistics";
		groupID = 1761; // see Reporter for more information about groupID
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
		if (source instanceof desmoj.core.statistic.BoolStatistic) {
			// the Tally we report about (source = informationSource)
			desmoj.core.statistic.BoolStatistic bs = (desmoj.core.statistic.BoolStatistic) source;

			// Title
			entries[0] = bs.getName();
			// (Re)set
			entries[1] = bs.resetAt().toString();
			// Obs
			entries[2] = Long.toString(bs.getObservations());
            // Obs true
            entries[3] = Long.toString(bs.getTrueObs());
            // True ratio
			if (bs.getObservations() == 0) {
				entries[4] = " ";
			} else {
				entries[4] = Double.toString(bs.getTrueRatio());
			}
            
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class TallyReporter
