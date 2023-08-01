package desmoj.core.advancedModellingFeatures.report;

/**
 * Captures all relevant information about the Bin.
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

public class BinReporter extends desmoj.core.report.Reporter {

	// ****** methods ******

	/**
	 * Constructor for a new BinReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Bin are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The Bin to report about
	 */
	public BinReporter(desmoj.core.simulator.Reportable informationSource) {
		super(informationSource); // make a Reporter

		numColumns = 14;
		columns = new String[numColumns];
		columns[0] = "Title";
		columns[1] = "Order";
		columns[2] = "pass";
		columns[3] = "(Re)set";
		columns[4] = "Prod";
		columns[5] = "Cons";
		columns[6] = "Init";
		columns[7] = "Max";
		columns[8] = "Now";
		columns[9] = "Averg.";
		columns[10] = "avg.Wait";
		columns[11] = "QLimit";
		columns[12] = "QMaxL";
		columns[13] = "refus.";
		groupHeading = "Bins";
		groupID = 1111; // see Reporter for more information about groupID
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
		if (source instanceof desmoj.core.advancedModellingFeatures.Bin) {
			// the Bin we report about
			desmoj.core.advancedModellingFeatures.Bin b = (desmoj.core.advancedModellingFeatures.Bin) source; // source
			// =
			// informationSource

			// Title
			entries[0] = b.getName();
			// order
			entries[1] = b.getQueueStrategy();
			// pass
			String pass = "no";
			if (b.getPassBy()) {
				pass = "yes";
			}
			entries[2] = pass;
			// (Re)set
			entries[3] = b.resetAt().toString();
			// Prod
			entries[4] = Long.toString(b.getProducers());
			// Cons
			entries[5] = Long.toString(b.getConsumers());
			// Init
			entries[6] = Long.toString(b.getInitial());
			// Max
			entries[7] = Long.toString(b.getMaximum());
			// Now.
			entries[8] = Long.toString(b.getAvail());
			// Average
			entries[9] = Double.toString(b.avgAvail());
			// avg.Wait
			entries[10] = b.averageWaitTime().toString();
			// Qlimit
			entries[11] = Long.toString(b.getQueueLimit());
			if (b.getQueueLimit() == Integer.MAX_VALUE) {
				entries[11] = "unlimit.";
			}
			// QMaxL
			entries[12] = Long.toString(b.maxLength());
			// refused
			entries[13] = Long.toString(b.getRefused());
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		} // end else

		return entries;
	}
} // end class BinReporter
