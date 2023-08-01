package desmoj.core.report;

import desmoj.core.simulator.ProcessQueue;

/**
 * Captures all relevant information about a ProcessQueue.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
 * @author modified by Soenke Claassen
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
public class ProcessQueueReporter extends Reporter {
	/**
	 * Creates a new ProcessQueueReporter. Note that although any Reportable is
	 * accepted you should make sure that only subtypes of Queue are passed to
	 * this constructor. Otherwise the number of column titles and their
	 * individual headings will differ from the actual content collected by this
	 * reporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The queue to report about
	 */
	public ProcessQueueReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

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
        columns[9] = "max.Wait";
        columns[10] = "avg.Wait";
        columns[11] = "refus.";
		groupHeading = "Queues";
		groupID = 251;
		entries = new String[numColumns];
	}

	/**
	 * Returns an array of Strings each containing the data for the
	 * corresponding column in array <code>columns[]</code>. Implement this
	 * method tha an array of the same langth as the columntitles is produced
	 * containing the data at the point of time this method is called by someone
	 * else to produce up-to-date information.
	 * 
	 * @return java.lang.String[] : Array containing the data for reporting
	 */
	public String[] getEntries() {

		if (source instanceof ProcessQueue) {
			// variable valid for block only - for faster access
			ProcessQueue<?> q = (ProcessQueue<?>) source;

			// Title
			entries[0] = source.getName();
			// order
			entries[1] = q.getQueueStrategy();
			// (Re)set
			entries[2] = source.resetAt().toString();
			// Obs
			entries[3] = Long.toString(source.getObservations());
			// Qlimit
			entries[4] = Long.toString(q.getQueueLimit());
			if (q.getQueueLimit() == Integer.MAX_VALUE) {
				entries[4] = "unlimit.";
			}
			// Qmax
			entries[5] = Long.toString(q.maxLength());
			// Qnow
			entries[6] = Long.toString(q.length());
			// Qavg.
			entries[7] = Double.toString(q.averageLength());
			// Zeros
			entries[8] = Long.toString(q.zeroWaits());
            // max.Wait
            entries[9] = q.maxWaitTime().toString();
            // avg.Wait
            entries[10] = q.averageWaitTime().toString();
            // refused
            entries[11] = Long.toString(q.getRefused());
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			} // end for
		}

		return entries;
	}
}