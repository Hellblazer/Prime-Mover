package desmoj.core.report;

/**
 * The basic reporter for all distributions in DESMO-J. All reporters producing
 * output about a special distribution have to extend this reporter to add the
 * special values to its report.
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
public class DistributionReporter extends Reporter {
	/**
	 * DistributionReporter - Konstruktorkommentar.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable
	 */
	public DistributionReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		numColumns = 8;
		columns = new String[numColumns];
		entries = new String[numColumns];
		groupID = 100; // low groupID, so always last in order

		columns[0] = "Title";
		columns[1] = "(Re)set";
		columns[2] = "Obs";
		columns[3] = "Type";
		columns[4] = "Parameter 1";
		columns[5] = "Parameter 2";
		columns[6] = "Parameter 3";
		columns[7] = "Seed";
		groupHeading = "Distributions";

	}

	/**
	 * Returns the array of strings containing all information about the
	 * distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.Distribution) {
			// Title
			entries[0] = source.getName();
			// (Re)set
			entries[1] = source.resetAt().toString();
			// Obs
			entries[2] = Long.toString(source.getObservations());
			// Type
			entries[3] = "unnamed distribution";
			// param1
			entries[4] = " ";
			// param2
			entries[5] = " ";
			// param3
			entries[6] = " ";
			// seed
			entries[7] = Long.toString(((desmoj.core.dist.Distribution) source)
					.getInitialSeed());

		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}
		}

		return entries;

	}
}