package desmoj.core.report;

// TODO: Auto-generated Javadoc

/**
 * Reports all information about a EntityDistEmpirical distribution.
 *
 * @author Tim Lechler, Johannes G&ouml;bel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 */
public class EntityDistEmpiricalReporter extends DistributionReporter {
    /**
     * Creates a new EntityDistEmpiricalReporter.
     *
     * @param informationSource desmoj.core.simulator.Reportable : The EntityDistEmpirical distribution to report about
     */
    public EntityDistEmpiricalReporter(desmoj.core.simulator.Reportable informationSource) {

        super(informationSource);

        groupID = 162;

    }

    /**
     * Returns the array of strings containing all information about the EntityDistEmpirical distribution.
     *
     * @return java.lang.String[] : The array of Strings containing all information about the EntityDistEmpirical
     * distribution
     */
    @Override
    public java.lang.String[] getEntries() {

        if (source instanceof desmoj.core.dist.EntityDistEmpirical<?>) {
            // use casted ide as a shortcut for source
            desmoj.core.dist.EntityDistEmpirical<?> idu = (desmoj.core.dist.EntityDistEmpirical<?>) source;
            // Title
            entries[0] = idu.getName();
            // (Re)set
            entries[1] = idu.resetAt().toString();
            // Obs
            entries[2] = Long.toString(idu.getObservations());
            // Type
            entries[3] = "Entity Empirical";
            // param1
            entries[4] = " ";
            // param2
            entries[5] = " ";
            // param3
            entries[6] = " ";
            // seed
            entries[7] = Long.toString(idu.getInitialSeed());
        } else {
            for (int i = 0; i < numColumns; i++) {
                entries[i] = "Invalid source!";
            }
        }

        return entries;

    }
}
