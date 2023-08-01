package desmoj.core.report;

import desmoj.core.dist.DiscreteDistEmpirical;


/**
 * Distribution returning empirical distributed double values.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Peter Wueppen
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

public class DiscreteDistEmpiricalReporter extends DistributionReporter {
	/**
	 * Creates a new DiscreteDistEmpiricalReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The EmpiricalDiscreteDist distribution to report
	 *            about
	 */
	public DiscreteDistEmpiricalReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 172;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * EmpiricalDiscreteDist distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the EmpiricalDiscreteDist distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.DiscreteDistEmpirical<?>) {

			// use casted ide as a shortcut for source
			DiscreteDistEmpirical<?> td = (DiscreteDistEmpirical<?>) source;
			// Title
			entries[0] = td.getName();
			// (Re)set
			entries[1] = td.resetAt().toString();
			// Obs
			entries[2] = Long.toString(td.getObservations());
			// Type
			entries[3] = "Discrete Empirical";
			// param1
			entries[4] = " ";
			// param2
			entries[5] = " ";
			// param3
			entries[6] = " ";
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
