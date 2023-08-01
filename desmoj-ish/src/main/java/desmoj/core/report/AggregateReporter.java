package desmoj.core.report;

import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Aggregate;

/**
 * Captures all relevant information about the Aggregate.
 * Extended to show unit and description of reported object.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen based on ideas from Tim Lechler
 * @author based on DESMO-C from Thomas Schniewind, 1998
 * @author modified by Ruth Meyer, Johannes G&ouml;bel
 * @author modified by Chr. M&uumlller (TH Wildau) 28.11.2012
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

public class AggregateReporter extends desmoj.core.report.Reporter {
    
	// ****** methods ******

	/**
	 * Constructor for a new AggregateReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Aggregate are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Aggregate to report about
	 */

	public AggregateReporter(Reportable informationSource) {
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
		if (source instanceof Aggregate) {
			// the Aggregate we report about
			Aggregate agg = (Aggregate) source;
			boolean _showTimeSpansInReport     = agg.getShowTimeSpansInReport();
			
			// source = informationSource
			// Title
			entries[0] = agg.getName();
            // Type
            entries[1] = "Aggregate";
			// (Re)set
			entries[2] = agg.resetAt().toString();
			// Observations
			entries[3] = Long.toString(agg.getObservations());
			// current value
			entries[4] = this.format(_showTimeSpansInReport, agg.getValue());
			// Min
			entries[5] = this.format(_showTimeSpansInReport, agg.getMinimum());
			// Max
			entries[6] = this.format(_showTimeSpansInReport, agg.getMaximum());
			//cm 21.11.12  Extension for viewing unit
			entries[7] = agg.getUnitText();

		}

		else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
	
    private String format(boolean showTimeSpans, double value){
        String out = Double.toString(value);
        if(showTimeSpans && value < 0.0)                    out += " (Invalid)";
        else if(showTimeSpans && value >= Long.MAX_VALUE)   out += " (Invalid)";
        else if(showTimeSpans)                              out = new TimeSpan(value).toString();
        return out;
    }
} // end class CountReporter
