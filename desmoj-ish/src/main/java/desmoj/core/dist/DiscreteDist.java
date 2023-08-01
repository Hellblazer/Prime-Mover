package desmoj.core.dist;

import desmoj.core.simulator.Model;

/**
 * Superclass for all distributions returning discrete samples of arbitrary 
 * numerical type.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Tim Lechler, Johannes Goebel
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
public abstract class DiscreteDist<N extends Number> extends
		NumericalDist<N> {

    /**
     * Constructs a distribution returning discretely distributed samples
     * of a custom type. Note that the method <code>N sample()</code> returning the samples 
     * (inherited from <code>NumericalDist<N></code>) has to be
     * implemented in subclasses.
     * 
     * @param owner
     *            Model : The distribution's owner
     * @param name
     *            java.lang.String : The distribution's name
     * @param showInReport
     *            boolean : Flag to show distribution in report
     * @param showInTrace
     *            boolean : Flag to show distribution in trace
     */
	public DiscreteDist(Model owner, String name, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}
}