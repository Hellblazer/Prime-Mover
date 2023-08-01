package desmoj.core.report;

/**
 * Reports all information about a ContDistUniform distribution.
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
public class ContDistUniformReporter extends DistributionReporter {
	/**
	 * Creates a new ContDistUniformReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The ContDistUniform distribution to report
	 *            about
	 */
	public ContDistUniformReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 155;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistUniform distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistUniform distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.ContDistUniform) {

			// use casted ide as a shortcut for source
			desmoj.core.dist.ContDistUniform rdn = (desmoj.core.dist.ContDistUniform) source;
			// Title
			entries[0] = rdn.getName();
			// (Re)set
			entries[1] = rdn.resetAt().toString();
			// Obs
			entries[2] = Long.toString(rdn.getObservations());
			// Type
			entries[3] = "Cont Uniform";
			// param1
			entries[4] = Double.toString(rdn.getLower());
			// param2
			entries[5] = Double.toString(rdn.getUpper());
            // param3
            entries[6] = " ";
            // seed
			entries[7] = Long.toString(rdn.getInitialSeed());

		} else {

			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}

		}

		return entries;

	}
}