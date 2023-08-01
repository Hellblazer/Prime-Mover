package desmoj.core.report;

/**
 * Reports all information about a ContDistExponential 
 * (or ContDistweibull) distribution.
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
public class ContDistExponReporter extends DistributionReporter {
	/**
	 * Creates a new ContDistExponentialReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The ContDistExponential distribution to
	 *            report about
	 */
	public ContDistExponReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 153;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistExponential distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistExponential distribution
	 */
	public java.lang.String[] getEntries() {
	    
	    if (source instanceof desmoj.core.dist.ContDistWeibull) {

            // use casted ide as a shortcut for source
            desmoj.core.dist.ContDistWeibull rdw = (desmoj.core.dist.ContDistWeibull) source;
            // Title
            entries[0] = rdw.getName();
            // (Re)set
            entries[1] = rdw.resetAt().toString();
            // Obs
            entries[2] = Long.toString(rdw.getObservations());
            // Type
            entries[3] = "Cont Weibull";
            // param1
            entries[4] = Double.toString(rdw.getMean());
            // param2
            entries[5] = Double.toString(rdw.getBeta());
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(rdw.getInitialSeed());

        } else if (source instanceof desmoj.core.dist.ContDistExponential) {

			// use casted ide as a shortcut for source
			desmoj.core.dist.ContDistExponential rdx = (desmoj.core.dist.ContDistExponential) source;
			if (source instanceof desmoj.core.dist.ContDistWeibull) {
			    desmoj.core.dist.ContDistWeibull rdw = (desmoj.core.dist.ContDistWeibull) rdx;
			}
			// Title
			entries[0] = rdx.getName();
			// (Re)set
			entries[1] = rdx.resetAt().toString();
			// Obs
			entries[2] = Long.toString(rdx.getObservations());
			// Type
			entries[3] = "Cont Exponential";
			// param1
			entries[4] = Double.toString(rdx.getMean());
            // param2
            entries[5] = " ";
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(rdx.getInitialSeed());

		} else {

			for (int i = 0; i < numColumns; i++) {
				entries[i] = "Invalid source!";
			}

		}

		return entries;

	}
}