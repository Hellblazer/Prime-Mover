package desmoj.core.report;

// TODO: Auto-generated Javadoc
/**
 * Reports all information about a EntityDistUniform distribution.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Johannes G&ouml;bel
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
public class EntityDistUniformReporter extends DistributionReporter {
    /**
     * Creates a new EntityDistUniformReporter.
     * 
     * @param informationSource
     *            desmoj.core.simulator.Reportable : The EntityDistUniform distribution to report
     *            about
     */
    public EntityDistUniformReporter(
            desmoj.core.simulator.Reportable informationSource) {

        super(informationSource);

        groupID = 161;

    }

    /**
     * Returns the array of strings containing all information about the
     * EntityDistUniform distribution.
     * 
     * @return java.lang.String[] : The array of Strings containing all
     *         information about the EntityDistUniform distribution
     */
    public java.lang.String[] getEntries() {

        if (source instanceof desmoj.core.dist.EntityDistUniform<?>) {
            // use casted ide as a shortcut for source
            desmoj.core.dist.EntityDistUniform<?> idu = (desmoj.core.dist.EntityDistUniform<?>) source;
            // Title
            entries[0] = idu.getName();
            // (Re)set
            entries[1] = idu.resetAt().toString();
            // Obs
            entries[2] = Long.toString(idu.getObservations());
            // Type
            entries[3] = "Entity Uniform";
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