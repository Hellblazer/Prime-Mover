package desmoj.core.report;

import java.util.ArrayList;
import java.util.List;

import desmoj.core.simulator.NamedObject;

/**
 * Controls all reports given by reportable model components used during an
 * experiment.
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
public class ReportManager extends NamedObject {

	/**
	 * Keeps references to all Reporters of this experiment
	 */
	private ArrayList<Reporter> _reporters;

	/**
	 * Creates a new reportmanager with the given name.
	 * 
	 * @param name
	 *            java.lang.String : the reportmanager's name
	 */
	public ReportManager(String name) {

		super(name + "_ReportManager"); // create the NamedObject

		_reporters = new ArrayList<Reporter>(); // init list for reporters

	}

	/**
	 * Adds a report to the very end of the vector hanlded by this
	 * reportmanager. This is needed to place submodel reporters behind existing
	 * model reporters to prevent multiple models to be mixed.
	 * 
	 * @param r
	 *            desmoj.report.Reporter
	 */
	public void addLast(Reporter r) {

		if (r == null)
			return;
		else
			_reporters.add(r);

	}

	/**
	 * De-registers the given reporter from the experiment's reportmanager. The
	 * reporter will be removed from the list of current available reporters and
	 * thus will not produce output whenever a report has to be produced. When
	 * de-registering, the order according to group-ID is preserved. If an
	 * invalid parameter is given (i.e. a <code>null</code> reference) this
	 * method simply returns
	 * 
	 * @param rep
	 *            desmoj.report.Reporter : The reporter to be de-registered
	 */
	public void deRegister(Reporter rep) {

		// check parameter
		if (rep == null)
			return; // wrong parameters ???
		_reporters.remove(rep); // no check if contained or not
		// necessary

	}

	/**
	 * Returns a list view of all registered reporters in the appropriate
	 * order.
	 * 
	 * @return java.util.List : The list of reporters
	 *         registered at the reportmanager
	 */
	public List<Reporter> elements() {

		return new ArrayList<Reporter>(_reporters);

	}

	/**
	 * Returns a boolean value indicating whether this reportmanager contains
	 * reporters to be sent to the reportoutput or not.
	 * 
	 * @return boolean : Is <code>true</code> if the reportmanager conatains
	 *         reporters, <code>false</code> otherwise
	 */
	public boolean isEmpty() {

		return _reporters.isEmpty();

	}

	/**
	 * Registers the given reporter at the experiment's reportmanager. All
	 * reporters registered will be sent to the reportout whenever a report has
	 * to be produced. When registering, the order according to group-ID is
	 * preserved.
	 * 
	 * @param rep
	 *            desmoj.report.Reporter : The reporter to be registered
	 */
	public void register(Reporter rep) {

		// check parameter
		if (rep == null)
			return; // wrong parameter

		if (_reporters.contains(rep))
			return; // already listed

		// register rep with special care for fist element
		if (_reporters.isEmpty())
			_reporters.add(rep);
		else {

			for (int i = 0; i < _reporters.size(); i++) {

				if (Reporter.isLarger(rep, _reporters.get(i))) {
					_reporters.add(i, rep);
					return;
				}

			}

			// if come to here, it must be smaller than all other reporters in
			// vector
			_reporters.add(rep);

		}

	}
}