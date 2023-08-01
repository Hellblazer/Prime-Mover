package desmoj.core.dist;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * A series is a special distribution returning preset, user-defined entries 
 * from a list. This subclass of Series serves to sample entities. 
 * Series may be used to simulate certain non-random scenarios 
 * within the simulation or to include external sources of (preudo) 
 * random distributions<p>
 * 
 * The internal list can be set to be traversed backwards and/or to repeat once 
 * its end has been reached.
 * 
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 * @author Broder Fredrich
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
public class SeriesEntities<E extends Entity> extends Series<E> {

	/**
	 * Creates a new SeriesEntities. Default behaviour when returning samples is -
	 * starting at first element - reading forward - non-repeating
	 * 
	 * @param owner
	 *            Model : The distribution's owner
	 * @param name
	 *            java.lang.String : The distribution's name
	 * @param showInReport
	 *            boolean : Flag for producing reports
	 * @param showInTrace
	 *            boolean : Flag for producing trace output
	 */
	public SeriesEntities(Model owner, String name, boolean showInReport,
			boolean showInTrace) {
		super(owner, name, showInReport, showInTrace);
	}
}
