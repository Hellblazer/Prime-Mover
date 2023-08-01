package desmoj.core.report;

/**
 * Reports all information about a DiscreteDistPoisson distribution.
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
public class DiscreteDistPoissonReporter extends DistributionReporter {
	/**
	 * Creates a new DiscreteDistPoissonReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The discrete Poisson distribution to report
	 *            about
	 */
	public DiscreteDistPoissonReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 124;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * DiscreteistPoisson distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the DiscreteDistPoisson distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.DiscreteDistPoisson) {
			// use casted ide as a shortcut for source
			desmoj.core.dist.DiscreteDistPoisson idp = (desmoj.core.dist.DiscreteDistPoisson) source;
			// Title
			entries[0] = idp.getName();
			// (Re)set
			entries[1] = idp.resetAt().toString();
			// Obs
			entries[2] = Long.toString(idp.getObservations());
			// Type
			entries[3] = "Discrete Poisson";
			// param1
			entries[4] = Double.toString(idp.getMean());
			// param2
			entries[5] = " ";
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(idp.getInitialSeed());		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}
		}

		return entries;

	}
}