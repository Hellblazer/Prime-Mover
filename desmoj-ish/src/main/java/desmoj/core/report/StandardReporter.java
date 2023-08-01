package desmoj.core.report;

/**
 * The standard reporter for any reportable object. Its report contains the
 * reportable's name, the simulation time of the last reset and the number of
 * observations made by the reportable. These represent the basic data each
 * reportable can supply. To get more specific information, build a custom
 * reporter for that reportable.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public class StandardReporter extends Reporter {
	/**
	 * Constructs a standrad reporter to report about the given reportable.
	 * Reports produced by this standard reporter are always listed last on a
	 * report output.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The reportable to produce a report about
	 */
	public StandardReporter(desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = -2147483648; // lowest possible groupID to display last in
		// report
		groupHeading = "StandardReporter";

		numColumns = 3;

		columns = new String[numColumns];
		entries = new String[numColumns];

		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";

	}

	/**
	 * Returns the array of strings containing the basic information any
	 * reportable can offer.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the reportable information source
	 */
	public java.lang.String[] getEntries() {

     // if (source instanceof desmoj.core.simulator.Reportable) {  // Replaced by test for non-null since instanceof always yields true (JG, 11.03.09)
		if (source != null) {

			// Title
			entries[0] = source.getName();
			// (Re)set
			entries[1] = source.resetAt().toString();
			// Observations
			entries[2] = Long.toString(source.getObservations());

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}
		}

		return entries;

	}
}