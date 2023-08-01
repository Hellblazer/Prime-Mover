package desmoj.core.report;

/**
 * Reports all information about a BoolDistBernoulli distribution.
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
public class BoolDistBernoulliReporter extends DistributionReporter {
	/**
	 * Creates a new BoolDistBernoulliReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The BoolDistBernoulli distribution to
	 *            report about
	 */
	public BoolDistBernoulliReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 102;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * BoolDistBernoulli distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the BoolDistBernoulli distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.BoolDistBernoulli) {
			// use casted bdb as a shortcut for source
			desmoj.core.dist.BoolDistBernoulli bdb = (desmoj.core.dist.BoolDistBernoulli) source;
			// Title
			entries[0] = bdb.getName();
			// (Re)set
			entries[1] = bdb.resetAt().toString();
			// Obs
			entries[2] = Long.toString(bdb.getObservations());
			// Type
			entries[3] = "Bool Bernoulli";
			// param1
			entries[4] = Double.toString(bdb.getProbability());
			// param2
			entries[5] = " ";
			// seed
			entries[6] = " ";
            // seed
            entries[7] = Long.toString(bdb.getInitialSeed());
		} else {
			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}
		}

		return entries;

	}
}