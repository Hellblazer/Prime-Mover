package desmoj.core.advancedModellingFeatures.report;

/**
 * Captures all relevant information about the CondQueue.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Soenke Claassen
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

public class CondQueueReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new CondQueueReporter. Note that although any
	 * Reportable is accepted you should make sure that only subtypes of
	 * CondQueue are passed to this constructor. Otherwise the number of column
	 * titles and their individual headings will differ from the actual content
	 * collected by this reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The CondQueue to report about
	 */
	public CondQueueReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 12;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "Qorder";
		columns[2] = "(Re)set";
		columns[3] = "Obs";
		columns[4] = "QLimit";
		columns[5] = "Qmax";
		columns[6] = "Qnow";
		columns[7] = "Qavg.";
		columns[8] = "Zeros";
		columns[9] = "avg.Wait";
		columns[10] = "refus.";
		columns[11] = "All";
		groupHeading = "Cond-Queues";
		groupID = 711; // see Reporter for more information about groupID
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
		if (source instanceof desmoj.core.advancedModellingFeatures.CondQueue) {
			// the CondQueue we report about
			desmoj.core.advancedModellingFeatures.CondQueue cq = (desmoj.core.advancedModellingFeatures.CondQueue) source;
			// source = informationSource
			// Title
			entries[0] = cq.getName();
			// Qorder
			entries[1] = cq.getQueueStrategy();
			// (Re)set
			entries[2] = cq.resetAt().toString();
			// Obs
			entries[3] = Long.toString(cq.getObservations());
			// Qlimit
			entries[4] = Long.toString(cq.getQueueLimit());
			if (cq.getQueueLimit() == Integer.MAX_VALUE) {
				entries[4] = "unlimit.";
			}
			// Qmax
			entries[5] = Long.toString(cq.maxLength());
			// Qnow
			entries[6] = Long.toString(cq.length());
			// Qavg..
			entries[7] = Double.toString(cq.averageLength());
			// Zeros
			entries[8] = Long.toString(cq.zeroWaits());
			// avg.Wait
			entries[9] = cq.averageWaitTime().toString();
			// refused
			entries[10] = Long.toString(cq.getRefused());
			// All
			entries[11] = String.valueOf(cq.getCheckAll());
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class CondQueueReporter
