package desmoj.core.report;

/**
 * Reports all information about a ContDistAggregate distribution.
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
public class ContDistAggregateReporter extends DistributionReporter {
	/**
	 * Creates a new ContDistAggregateReporter.
	 * 
	 * @param informationSource
	 *            desmoj.core.simulator.Reportable : The ContDistAggregate distribution to report
	 *            about
	 */
	public ContDistAggregateReporter(
			desmoj.core.simulator.Reportable informationSource) {

		super(informationSource);

		groupID = 175;

	}

	/**
	 * Returns the array of strings containing all information about the
	 * ContDistAggregate distribution.
	 * 
	 * @return java.lang.String[] : The array of Strings containing all
	 *         information about the ContDistAggregate distribution
	 */
	public java.lang.String[] getEntries() {

		if (source instanceof desmoj.core.dist.ContDistAggregate) {

			// use casted ide as a shortcut for source
			desmoj.core.dist.ContDistAggregate td = (desmoj.core.dist.ContDistAggregate) source;
			// Title
			entries[0] = td.getName();
			// (Re)set
			entries[1] = td.resetAt().toString();
			// Obs
			entries[2] = Long.toString(td.getObservations());
			// Type
			entries[3] = "Cont Aggregate";
			// param1
			entries[4] = td.getDists().get(0).getName();
			// param2
			entries[5] = (td.getDists().size() > 1 ? td.getDists().get(1).getName() : "") + (td.getDists().size() > 2 ? ", ..." : "");
			// param3
			entries[6] = td.getOperator().getDescription();
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
