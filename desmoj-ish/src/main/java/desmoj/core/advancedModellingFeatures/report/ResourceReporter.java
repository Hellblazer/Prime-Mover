package desmoj.core.advancedModellingFeatures.report;

import desmoj.core.statistic.StatisticObject;

/**
 * Captures all relevant information about the Res.
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

public class ResourceReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new ResourceReporter. Note that although any Reportable
	 * is accepted you should make sure that only subtypes of Res are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Res to report about.
	 */
	public ResourceReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 14;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "Order";
		columns[2] = "pass";
		columns[3] = "(Re)set";
		columns[4] = "Users";
		columns[5] = "Limit";
		columns[6] = "Min";
		columns[7] = "Now";
		columns[8] = "Usage[%]";
		columns[9] = "avg.Wait";
		columns[10] = "QLimit";
		columns[11] = "QMaxL";
		columns[12] = "refus.";
		columns[13] = "DL";
		groupHeading = "Resources";
		groupID = 811; // see Reporter for more information about groupID
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
		if (source instanceof desmoj.core.advancedModellingFeatures.Res) {
			// the Res we report about
			desmoj.core.advancedModellingFeatures.Res rs = (desmoj.core.advancedModellingFeatures.Res) source; // source
			// =
			// informationSource

			// Title
			entries[0] = rs.getName();
			// order
			entries[1] = rs.getQueueStrategy();
			// pass
			String pass = "no";
			if (rs.getPassBy()) {
				pass = "yes";
			}
			entries[2] = pass;
			// (Re)set
			entries[3] = rs.resetAt().toString();
			// Users
			entries[4] = Long.toString(rs.getUsers());
			// Limit
			entries[5] = Long.toString(rs.getLimit());
			// Min
			entries[6] = Long.toString(rs.getMinimum());
			// Now.
			entries[7] = Long.toString(rs.getAvail());
			// Usage[%]
			// round to percentage
			entries[8] = Double.toString(StatisticObject.round(rs.avgUsage() * 100));
			// avg.Wait
			entries[9] = rs.averageWaitTime().toString();
			// Qlimit
			entries[10] = Long.toString(rs.getQueueLimit());
			if (rs.getQueueLimit() == Integer.MAX_VALUE) {
				entries[10] = "unlimit.";
			}
			// QMaxL
			entries[11] = Long.toString(rs.maxLength());
			// refused
			entries[12] = Long.toString(rs.getRefused());
			// DL
			String deadLock = "no";
			if (rs.isDeadlockDetected()) {
				deadLock = "yes";
			}
			entries[13] = deadLock;
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class ResourceReporter
