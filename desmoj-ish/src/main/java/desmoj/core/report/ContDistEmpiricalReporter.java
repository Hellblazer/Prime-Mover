package desmoj.core.report;

/**
 * Reports all information about a ContDistEmpirical distribution.
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
public class ContDistEmpiricalReporter extends DistributionReporter {
	
    /**
	 * Creates a new ContDistEmpiricalReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The ContDistEmpirical distribution to
	 *            report about
	 */
	public ContDistEmpiricalReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 152;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistEmpirical distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistEmpirical distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.ContDistEmpirical) {

			// use casted ide as a shortcut for source
			desmoj.core.dist.ContDistEmpirical rde = (desmoj.core.dist.ContDistEmpirical) source;
			// Title
			entries[0] = rde.getName();
			// (Re)set
			entries[1] = rde.resetAt().toString();
			// Obs
			entries[2] = Long.toString(rde.getObservations());
			// Type
			entries[3] = "Cont Empirical";
			// param1
			entries[4] = " ";
			// param2
			entries[5] = " ";
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(rde.getInitialSeed());
		} else {

			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}

		}

		return entries;

	}
}