package desmoj.core.report;

/**
 * Reports all information about a ContDistTriangular distribution.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Peter Wueppen
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
public class ContDistTriangularReporter extends DistributionReporter {
	/**
	 * Creates a new ContDistTriangularReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The TriangularDist distribution to report
	 *            about
	 */
	public ContDistTriangularReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 170;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistTriangular distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistTriangular distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.ContDistTriangular) {

			// use casted ide as a shortcut for source
			desmoj.core.dist.ContDistTriangular td = (desmoj.core.dist.ContDistTriangular) source;
			// Title
			entries[0] = td.getName();
			// (Re)set
			entries[1] = td.resetAt().toString();
			// Obs
			entries[2] = Long.toString(td.getObservations());
			// Type
			entries[3] = "Cont Triangular";
			// param1
			entries[4] = Double.toString(td.getLower());
			// param2
			entries[5] = Double.toString(td.getUpper());
			// param3
			entries[6] = Double.toString(td.getPeak());
			// seed
			entries[7] = Long.toString(td.getInitialSeed());

		} else {

			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}

		}

		return entries;

	}
}
