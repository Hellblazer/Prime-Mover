package desmoj.core.report;

/**
 * Reports all information about a CustomContDist distribution.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler
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
public class ContDistCustomReporter extends DistributionReporter {
	/**
	 * Creates a new ContDistCustomReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The CustomContDist distribution to
	 *            report about
	 */
	public ContDistCustomReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 173;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistCustom distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistCustom distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.ContDistCustom) {
			// use casted bdb as a shortcut for source

			desmoj.core.dist.ContDistCustom cdd = (desmoj.core.dist.ContDistCustom) source;
			// Title
			entries[0] = cdd.getName();
			// (Re)set
			entries[1] = cdd.resetAt().toString();
			// Obs
			entries[2] = Long.toString(cdd.getObservations());
			// Type
			entries[3] = cdd.getFunction().getDescription();
			// param1
			entries[4] = Double.toString(cdd.getLowerBound());
			// param2
			entries[5] = Double.toString(cdd.getUpperBound());
			// param3
			entries[6] = " ";
			// seed
			entries[7] = Long.toString(cdd.getInitialSeed());
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}
		}

		return entries;

	}
}